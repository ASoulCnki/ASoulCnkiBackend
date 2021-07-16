package asia.asoulcnki.api.common.duplicationcheck;

import asia.asoulcnki.api.persistence.entity.Reply;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ComparisonDatabase {
	public static final String DEFAULT_IMAGE_PATH = "data/database.dat";
	private final static Logger log = LoggerFactory.getLogger(ComparisonDatabase.class);
	private static ComparisonDatabase instance;
	private transient ReadWriteLock rwLock;

	private int minTime;
	private int maxTime;
	private long maxRpid;
	// reply id -> reply
	private Map<Long, Reply> replyMap;
	//  text hash -> reply ids
	private Map<Long, ArrayList<Long>> textHashMap;

	private ComparisonDatabase() {
		this.maxRpid = 0;
		this.minTime = Integer.MAX_VALUE;
		this.maxTime = 0;
		this.rwLock = new ReentrantReadWriteLock();
		this.replyMap = new HashMap<>(70 * 10000);
		this.textHashMap = new HashMap<>(70 * 10000);
	}

	public static synchronized ComparisonDatabase getInstance() {
		if (instance == null) {
			synchronized (ComparisonDatabase.class) {
				if (instance == null) {
					try {
						long start = System.currentTimeMillis();
						log.info("start to load comparison database...");
						instance = loadFromImage(DEFAULT_IMAGE_PATH);
						log.info("load database cost {} ms", System.currentTimeMillis() - start);
					} catch (Exception e) {
						e.printStackTrace();
						instance = new ComparisonDatabase();
					}
					instance.rwLock = new ReentrantReadWriteLock();
				}
			}
		}
		return instance;
	}

	static void printMemory() {
		log.info("max memory {}", mb(Runtime.getRuntime().maxMemory()));
		log.info("total memory {}", mb(Runtime.getRuntime().totalMemory()));
		log.info("free memory {}", mb(Runtime.getRuntime().freeMemory()));
	}

	static String mb(long s) {
		return String.format("%d (%.2f M)", s, (double) s / (1024 * 1024));
	}

	private static ComparisonDatabase loadFromImage(String path) throws IOException {
		Kryo kryo = new Kryo();
		File file = new File(path);
		log.info("before alloc buffer");
		printMemory();
		Input input = new Input(new FileInputStream(file), 1024 * 1000 * 100);
		log.info("after alloc buffer");
		printMemory();
		ComparisonDatabase db = kryo.readObject(input, ComparisonDatabase.class);
		log.info("after de-serialize");
		printMemory();
		input.close();
		return db;
	}

	private void readLock() {
		this.rwLock.readLock().lock();
	}

	private void readUnLock() {
		this.rwLock.readLock().unlock();
	}

	public void writeLock() {
		this.rwLock.writeLock().lock();
	}

	public void writeUnLock() {
		this.rwLock.writeLock().unlock();
	}

	public void dumpToImage(String path) throws IOException {
		Kryo kryo = new Kryo();
		File file = new File(path);
		Output output = new Output(new FileOutputStream(file));
		kryo.writeObject(output, this);
		output.close();
	}

	public void reset() {
		this.writeLock();
		try {
			this.maxRpid = 0;
			this.minTime = Integer.MAX_VALUE;
			this.maxTime = 0;
			this.replyMap = new HashMap<>(70 * 10000);
			this.textHashMap = new HashMap<>(70 * 10000);
		} finally {
			this.writeUnLock();
		}
	}

	public  long count = 0L;
	//TODO:照抄代码，等待抽象出来
	private static boolean isHighSimilarity(int textLength, float similarity) {
		if (textLength > 250) {
			return similarity > 0.6;
		} else if (textLength > 150) {
			return similarity > 0.7;
		} else {
			return similarity > 0.8;
		}
	}

	public void addReplyData(Reply reply) {
		try{
		if (reply == null || replyMap.containsKey(reply.getRpid())) {
			return;
		}
		if (reply.getContent().codePointCount(0, reply.getContent().length()) < SummaryHash.DEFAULT_K) {
			return;
		}

		if (reply.getCtime() > this.maxTime) {
			this.maxTime = reply.getCtime();
		}
		if (reply.getCtime() < this.minTime) {
			this.minTime = reply.getCtime();
		}
		if (reply.getRpid() > this.maxRpid) {
			this.maxRpid = reply.getRpid();
		}

		ArrayList<Long> textHashList = SummaryHash.defaultHash(reply.getContent());





		//TODO:照抄代码，等待抽象出来
		Map<Long, Integer> replyHitMap = new HashMap<>();

		for (Long textHash : textHashList) {
			ArrayList<Long> hitReplyIds = this.searchHash(textHash);
			if (hitReplyIds != null) {
				for (long id : hitReplyIds) {
					if (replyHitMap.containsKey(id)) {
						int count = replyHitMap.get(id);
						replyHitMap.put(id, count + 1);
					} else {
						replyHitMap.put(id, 1);
					}
				}
			}
		}

		// TODO refactor related item to a pojo
		List<List<Object>> related = new ArrayList<>(textHashList.size() / 2);

		float threshHold = (float) ((float) textHashList.size() * 0.2);
		Comparator<Map.Entry<Long, Integer>> cmp = Map.Entry.comparingByValue();
		List<Map.Entry<Long, Integer>> sortedList =
				replyHitMap.entrySet().stream().filter(entry -> entry.getValue() > threshHold).sorted(cmp.reversed()).collect(Collectors.toList());

		StringBuilder allContentBuilder = new StringBuilder();

		for (Map.Entry<Long, Integer> entry : sortedList) {
			Reply _reply = this.getReply(entry.getKey());
			String content = _reply.getContent();
			float similarity = SummaryHash.compareArticle(reply.getContent(), content);
			if (similarity < 0.2) {
				continue;
			}
			allContentBuilder.append(content);
			related.add(Lists.newArrayList(similarity, _reply));
		}

		int textLength = reply.getContent().codePointCount(0, reply.getContent().length());
		// param -> left hand side and right hand side
		related.sort((lhs, rhs) -> {
			float lhsSimilarity = (float) lhs.get(0);
			int lhsCTime = ((Reply) lhs.get(1)).getCtime();

			float rhsSimilarity = (float) rhs.get(0);
			int rhsCTime = ((Reply) rhs.get(1)).getCtime();

			if (isHighSimilarity(textLength, lhsSimilarity) && isHighSimilarity(textLength, rhsSimilarity)) {
				return Integer.compare(lhsCTime, rhsCTime);
			}

			if (lhsSimilarity != rhsSimilarity) {
				return -Float.compare(lhsSimilarity, rhsSimilarity);
			} else {
				return Integer.compare(lhsCTime, rhsCTime);
			}
		});
		float allSimilarity = 0;
		String allContent = allContentBuilder.toString();
		if (!StringUtils.isBlank(allContent)) {
			allSimilarity = SummaryHash.compareArticle(reply.getContent(), allContentBuilder.toString());
		}
		// 标记是否抄袭和源rpid
		if(reply.getOrigin_rpid()==0L){
			reply.setOrigin_rpid(reply.getRpid());
		}
		if (isHighSimilarity(textLength, allSimilarity)){
			reply.setCopy(true);
			Reply _reply = (Reply) related.get(0).get(1);
			Reply origin_reply = this.getReply(_reply.getOrigin_rpid());
//			while(origin_reply.isCopy()){
			origin_reply = this.getReply(origin_reply.getOrigin_rpid());
//				System.out.println(origin_reply.getOrigin_rpid());
			reply.setOrigin_rpid(origin_reply.getRpid());
//			}
		}
		this.replyMap.put(reply.getRpid(), reply);
		for (final Long textHash : textHashList) {
			if (!textHashMap.containsKey(textHash)) {
				textHashMap.put(textHash, new ArrayList<>());
			}
			textHashMap.get(textHash).add(reply.getRpid());
		}
		System.out.println(count);
		count++;
		}
		catch (Error e){
			System.out.println(e);
		}
	}

	public Reply getReply(long rpid) {
		try {
			this.readLock();
			return replyMap.get(rpid);
		} finally {
			this.readUnLock();
		}
	}

	public ArrayList<Long> searchHash(long textHash) {
		try {
			this.readLock();
			return this.textHashMap.get(textHash);
		} finally {
			this.readUnLock();
		}
	}


	public ReadWriteLock getRwLock() {
		return rwLock;
	}

	public int getMinTime() {
		return minTime;
	}

	public int getMaxTime() {
		return maxTime;
	}

	public long getMaxRpid() {
		return maxRpid;
	}

}
