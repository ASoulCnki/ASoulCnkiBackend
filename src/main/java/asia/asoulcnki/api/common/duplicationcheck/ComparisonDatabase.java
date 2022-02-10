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
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ComparisonDatabase {
    /**
     * 数据库存储文件夹
     */
    public static final String DEFAULT_DATA_DIR = "data";
    /**
     * 数据库文件名
     */
    public static final String DEFAULT_IMAGE_FILE_NAME = "database.dat";
    private static final Logger log = LoggerFactory.getLogger(ComparisonDatabase.class);
    private static final int INITIAL_CAPACITY = 100 * 10000;
    private static final double SIZE_THRESHOLD_FACTOR = 0.2;
    private static final int MIN_HIT = 2;
    private static volatile ComparisonDatabase instance;
    /**
     * 数据库读写锁
     */
    private transient ReadWriteLock rwLock;

    /**
     * 数据库内小作文起始和终止时间
     */
    private int minTime;
    private int maxTime;
    /**
     * replyMap 记录的是 reply id 和 reply 的映射关系
     */
    private Map<Long, Reply> replyMap;
    /**
     * textHashMap 记录的是 text hash 和 reply id 的映射关系
     */
    private Map<Long, ArrayList<Long>> textHashMap;

    private ComparisonDatabase() {
        this.minTime = Integer.MAX_VALUE;
        this.maxTime = 0;
        this.rwLock = new ReentrantReadWriteLock();
        this.replyMap = new HashMap<>(INITIAL_CAPACITY);
        this.textHashMap = new HashMap<>(INITIAL_CAPACITY);
    }

    public static synchronized ComparisonDatabase getInstance() {
        if (instance == null) {
            synchronized (ComparisonDatabase.class) {
                if (instance == null) {
                    try {
                        long start = System.currentTimeMillis();
                        log.info("start to load comparison database...");
                        instance = loadFromImage(DEFAULT_DATA_DIR + File.separator + DEFAULT_IMAGE_FILE_NAME);
                        log.info("load database cost {} ms", System.currentTimeMillis() - start);
                    } catch (Exception e) {
                        log.warn("loading comparison database failed, use empty database.", e);
                        instance = new ComparisonDatabase();
                    }
                    instance.rwLock = new ReentrantReadWriteLock();
                }
            }
        }
        return instance;
    }

    static void printMemory() {
        String maxMemory = mb(Runtime.getRuntime().maxMemory());
        String totalMemory = mb(Runtime.getRuntime().totalMemory());
        String freeMemory = mb(Runtime.getRuntime().freeMemory());
        log.info("max memory {}", maxMemory);
        log.info("total memory {}", totalMemory);
        log.info("free memory {}", freeMemory);
    }

    static String mb(long s) {
        return String.format("%d (%.2f M)", s, (double) s / (1024 * 1024));
    }

    /**
     * 从本地文件中读取数据库到内存
     *
     * @param path 数据库文件路径
     * @return 数据库实例
     * @throws IOException IO错误
     */
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

    /**
     * 从数据库中检索相似小作文
     *
     * @param textHashList 源小作文的hash序列
     * @param minHit       最少命中hash次数(即两篇小作文需具有超过minHit个相同的hash才会入选)
     * @return key-> rpid, value -> hit count
     */
    public static List<Map.Entry<Long, Integer>> searchRelatedReplies(List<Long> textHashList, int minHit) {
        float threshold = (float) (textHashList.size() * SIZE_THRESHOLD_FACTOR);
        if (threshold <= minHit) {
            threshold = minHit;
        }
        return searchRelatedReplies(textHashList, threshold);
    }

    /**
     * 从数据库中检索相似小作文
     *
     * @param textHashList 源小作文的hash序列
     * @param threshold    两篇小作文相同hash/源小作文最小值
     * @return key-> rpid, value -> hit count
     */
    public static List<Map.Entry<Long, Integer>> searchRelatedReplies(List<Long> textHashList, float threshold) {
        Map<Long, Integer> replyHitMap = new HashMap<>();
        for (Long textHash : textHashList) {
            List<Long> hitReplyIds = ComparisonDatabase.getInstance().searchHash(textHash);
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
        Comparator<Map.Entry<Long, Integer>> cmp = Map.Entry.comparingByValue();
        return replyHitMap.entrySet().stream().filter(entry -> entry.getValue() > threshold).sorted(cmp.reversed()).collect(Collectors.toList());
    }

    public Map<Long, Reply> getReplyMap() {
        return replyMap;
    }

    public void readLock() {
        this.rwLock.readLock().lock();
    }

    public void readUnLock() {
        this.rwLock.readLock().unlock();
    }

    public void writeLock() {
        this.rwLock.writeLock().lock();
    }

    public void writeUnLock() {
        this.rwLock.writeLock().unlock();
    }

    public void dumpToImage(String dataDir, String imageName) throws IOException {
        File folder = new File(dataDir);
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }
        Kryo kryo = new Kryo();
        File file = new File(dataDir + File.separator + imageName);
        Output output = new Output(new FileOutputStream(file));
        kryo.writeObject(output, this);
        output.close();
    }

    public void reset() {
        this.writeLock();
        try {
            this.minTime = Integer.MAX_VALUE;
            this.maxTime = 0;
            this.replyMap = new HashMap<>(INITIAL_CAPACITY);
            this.textHashMap = new HashMap<>(INITIAL_CAPACITY);
        } finally {
            this.writeUnLock();
        }
    }

    public void addReplyData(Reply reply) {
        if (reply == null) {
            return;
        }

        Reply oldReply = replyMap.get(reply.getRpid());

        // update related like sum
        if (oldReply != null) {
            if (oldReply.getLikeNum() != reply.getLikeNum()) {
                updateRelatedLikeNum(reply, oldReply);
            }
            return;
        }

        String content = ArticleCompareUtil.trim(reply.getContent());
        int codePointCount = content.codePointCount(0, content.length());
        // content is too short or content is chessboard
        if (codePointCount < SummaryHash.DEFAULT_K || isChess(content)) {
            return;
        }

        // calculate text hashes
        ArrayList<Long> textHashList = SummaryHash.defaultHash(content);

        // calculate if
        if (codePointCount > 30) {
            calculateIf(reply, content, textHashList);
        }

        // add reply to map and update stats
        this.replyMap.put(reply.getRpid(), reply);

        if (reply.getCtime() > this.maxTime) {
            this.maxTime = reply.getCtime();
        }
        if (reply.getCtime() < this.minTime) {
            this.minTime = reply.getCtime();
        }
        if (reply.getOriginRpid() < 0) {
            reply.setSimilarLikeSum(reply.getLikeNum());
        }

        // add text hash to search database
        textHashList.forEach(textHash -> textHashMap.computeIfAbsent(textHash, key -> new ArrayList<>()).add(reply.getRpid()));

    }

    private void updateRelatedLikeNum(Reply reply, Reply oldReply) {
        if (oldReply.getOriginRpid() >= 0) { // old reply is copied from original reply
            Reply relatedReply = replyMap.get(oldReply.getOriginRpid());
            int likeSum = relatedReply.getSimilarLikeSum();
            likeSum = likeSum - oldReply.getLikeNum() + reply.getLikeNum();
            relatedReply.setSimilarLikeSum(likeSum);
        } else {
            oldReply.setSimilarLikeSum(oldReply.getSimilarLikeSum() - oldReply.getLikeNum() + reply.getLikeNum());
        }
        oldReply.setLikeNum(reply.getLikeNum());
    }

    /**
     * 判断小作文是否是下棋型
     *
     * @param content 小作文内容
     * @return 小作文是否是下棋型
     */
    private boolean isChess(String content) {
        // we don't process chess 😈
        return content.contains("┼┼┼┼┼┼┼┼") || content.contains("┏┯┯┯┯┯┯┯┯┯┓") || content.contains(
            "┏┯┯┯┯┯┯┯┯┯┯┯┯┯┯┯┯┓");
    }

    /**
     * 计算小作文被引用次数
     *
     * @param reply        reply对象
     * @param content      小作文内容
     * @param textHashList 小作文hash序列
     *                     将被引用次数存入传入的reply对象中
     */
    private void calculateIf(Reply reply, String content, List<Long> textHashList) {
        // key-> rpid, value -> hit count
        List<Map.Entry<Long, Integer>> sortedRelatedReplies = searchRelatedReplies(textHashList, MIN_HIT);

        for (Map.Entry<Long, Integer> entry : sortedRelatedReplies) {
            Reply relatedReply = this.getReply(entry.getKey());
            // find first original reply
            if (relatedReply.getOriginRpid() < 0) {
                String relatedContent = ArticleCompareUtil.trim(relatedReply.getContent());
                int relatedContentLength = ArticleCompareUtil.textLength(relatedContent);
                float similarity = ArticleCompareUtil.compareArticle(content, relatedContent);
                if (ArticleCompareUtil.isHighSimilarity(relatedContentLength, similarity)) {
                    reply.setOriginRpid(relatedReply.getRpid());
                    relatedReply.setSimilarCount(relatedReply.getSimilarCount() + 1);
                    relatedReply.setSimilarLikeSum(reply.getLikeNum() + relatedReply.getSimilarLikeSum());
                }
                break;
            }
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

    public List<Long> searchHash(long textHash) {
        try {
            this.readLock();
            return this.textHashMap.get(textHash);
        } finally {
            this.readUnLock();
        }
    }

    public int getMinTime() {
        return minTime;
    }

    public int getMaxTime() {
        return maxTime;
    }
}
