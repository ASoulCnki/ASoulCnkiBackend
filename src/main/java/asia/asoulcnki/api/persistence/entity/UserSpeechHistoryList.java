package asia.asoulcnki.api.persistence.entity;

import asia.asoulcnki.api.persistence.vo.UserSpeechHistoryVO;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class UserSpeechHistoryList implements Serializable {
    private static final long serialVersionUID = 5554064566476229771L;
    private List<UserSpeechHistory> histories;
    public static final String DEFAULT_DATA_DIR = "data";
    public static final String DEFAULT_IMAGE_FILE_NAME = "history.dat";
    private volatile static UserSpeechHistoryList instance;
    private final static Logger log = LoggerFactory.getLogger(UserSpeechHistoryList.class);

    private UserSpeechHistoryList() {
        this.histories = new ArrayList<>();
    }

    public static synchronized UserSpeechHistoryList getInstance() {
        if (instance == null) {
            synchronized (UserSpeechHistoryList.class) {
                if (instance == null) {
                    try {
                        long start = System.currentTimeMillis();
                        log.info("start to load user speech database...");
                        instance = loadFromImage(DEFAULT_DATA_DIR + "/" + DEFAULT_IMAGE_FILE_NAME);
                        log.info("load database cost {} ms", System.currentTimeMillis() - start);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                        log.warn("loading history database failed, use empty database.");
                        instance = new UserSpeechHistoryList();
                    }
                }
            }
        }
        return instance;
    }

    public void add(Reply reply) {
        if (UserSpeechHistory.KEYS.contains(reply.getMid())) {
            histories.get(getIndex(reply.getMid())).add(reply);
        } else {
            UserSpeechHistory history = new UserSpeechHistory(reply.getMid());
            history.add(reply);
            this.histories.add(history);
            this.histories.sort(Comparator.comparing(UserSpeechHistory::getMid));
        }
    }

    /**
     * 二分查找获取根据 mid 获取其在 UserSpeechHistory 列表中的下标
     *
     * @param id int
     * @return 下标
     */
    public int getIndex(int id) {
        int max = histories.size();
        int min = 0;
        int half = max / 2;
        while (min <= max) {
            if (histories.get(half).mid == id) {
                return half;
            } else if (histories.get(half).mid < id) {
                min = half + 1;
            } else {
                max = half - 1;
            }
            half = min + (max - min) / 2;
        }
        return -1;
    }

    public UserSpeechHistoryVO get(int id) {
        UserSpeechHistoryVO vo = new UserSpeechHistoryVO();
        int max = histories.size();
        System.err.println(max);
        int min = 0;
        int half = max / 2;
        while (min <= max) {
            if (histories.get(half).mid.equals(id)) {
                vo.setMid(histories.get(half).getMid());
                vo.setReplies(histories.get(half).getItems());
                return vo;
            } else if (histories.get(half).mid < id) {
                min = half + 1;
            } else {
                max = half - 1;
            }
            half = min + (max - min) / 2;
        }
        return vo;
    }

    // TODO: refactor and use oop
    private static UserSpeechHistoryList loadFromImage(String path) throws IOException {
        Kryo kryo = new Kryo();
        File file = new File(path);
        Input input = new Input(new FileInputStream(file), 1024 * 1000 * 100);
        UserSpeechHistoryList db = kryo.readObject(input, UserSpeechHistoryList.class);
        input.close();
        return db;
    }


    public void dumpToImage(String dataDir, String imageName) throws IOException {
        File folder = new File(dataDir);
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }
        Kryo kryo = new Kryo();
        File file = new File(dataDir + "/" + imageName);
        Output output = new Output(new FileOutputStream(file));
        kryo.writeObject(output, this);
        output.close();
    }


    @Data
    @ToString
    static class UserSpeechHistory implements Serializable {
        private static final long serialVersionUID = -1264694752077837892L;
        private Integer mid;
        private List<Reply> items;
        private static final Set<Integer> KEYS = new HashSet<>();

        public UserSpeechHistory(Integer mid) {
            this.mid = mid;
            this.items = new ArrayList<>();
            KEYS.add(mid);
        }

        public void add(Reply reply) {
            if (items.stream().noneMatch(f -> f.getCtime() == reply.getCtime())) {
                items.add(reply);
                items.sort(Comparator.comparing(Reply::getCtime));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UserSpeechHistory that = (UserSpeechHistory) o;
            return Objects.equals(mid, that.mid) && Objects.equals(items, that.items);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mid, items);
        }
    }
}
