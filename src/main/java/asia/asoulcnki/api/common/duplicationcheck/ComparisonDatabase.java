package asia.asoulcnki.api.common.duplicationcheck;

import asia.asoulcnki.api.persistence.entity.Reply;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ComparisonDatabase implements Serializable {
	private static final long serialVersionUID = -5543208627621913003L;
	private static final String IMAGE_PATH = "database.dat";
	private static ComparisonDatabase instance;
	private transient ReadWriteLock rwLock;
	private int minTime;
	private int maxTime;
	private long max_rpid;
	// reply id -> reply
	private Map<Long, Reply> replyMap;
	//  text hash -> reply ids
	private Map<Long, ArrayList<Long>> textHashMap;

	private ComparisonDatabase() {
		this.max_rpid = 0;
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
						instance = loadFromImage(IMAGE_PATH);
					} catch (Exception e) {
						instance = new ComparisonDatabase();
					}
					instance.rwLock = new ReentrantReadWriteLock();
				}
			}
		}
		return instance;
	}

	private static ComparisonDatabase loadFromImage(String path) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
		return (ComparisonDatabase) ois.readObject();
	}

	private void readLock() {
		this.rwLock.readLock().lock();
	}

	private void readUnLock() {
		this.rwLock.readLock().unlock();
	}

	private void writeLock() {
		this.rwLock.writeLock().lock();
	}

	private void writeUnLock() {
		this.rwLock.writeLock().unlock();
	}

	private void dumpToImage(String path) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
		oos.writeObject(this);
	}

	private void reset() {
		this.writeLock();
		try {
			this.max_rpid = 0;
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
		this.replyMap.put(reply.getRpid(), reply);

		if (reply.getCtime() > this.maxTime) {
			this.maxTime = reply.getCtime();
		}
		if (reply.getCtime() < this.minTime) {
			this.minTime = reply.getCtime();
		}
		if (reply.getRpid() > this.max_rpid) {
			this.max_rpid = reply.getRpid();
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
		return replyMap.get(rpid);
	}

	public ArrayList<Long> searchHash(long textHash) {
		return this.textHashMap.get(textHash);
	}


}
