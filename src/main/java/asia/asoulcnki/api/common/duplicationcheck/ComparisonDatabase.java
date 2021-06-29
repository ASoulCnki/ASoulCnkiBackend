package asia.asoulcnki.api.common.duplicationcheck;

import asia.asoulcnki.api.persistence.entity.Reply;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
		this.replyMap = new HashMap<>();
		this.textHashMap = new HashMap<>();
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
						instance = new ComparisonDatabase();
					}
					instance.rwLock = new ReentrantReadWriteLock();
				}
			}
		}
		return instance;
	}

	private static ComparisonDatabase loadFromImage(String path) throws IOException {
		Kryo kryo = new Kryo();
		File file = new File(path);
		Input input = new Input(new FileInputStream(file));
		ComparisonDatabase db = kryo.readObject(input, ComparisonDatabase.class);
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
			this.replyMap = new HashMap<>();
			this.textHashMap = new HashMap<>();
		} finally {
			this.writeUnLock();
		}
	}

	public void addReplyData(Reply reply) {
		if (reply == null || replyMap.containsKey(reply.getRpid())) {
			return;
		}
		if (reply.getContent().codePointCount(0, reply.getContent().length()) < SummaryHash.DEFAULT_K) {
			return;
		}
		this.replyMap.put(reply.getRpid(), reply);

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

		for (final Long textHash : textHashList) {
			if (!textHashMap.containsKey(textHash)) {
				textHashMap.put(textHash, new ArrayList<>());
			}
			textHashMap.get(textHash).add(reply.getRpid());
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
