package asia.asoulcnki.api.persistence.entity;

import asia.asoulcnki.api.persistence.vo.UserSpeechHistoryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class UserSpeechHistoryList implements Serializable {
    private static final long serialVersionUID = 5554064566476229771L;
    private List<UserSpeechHistory> histories;

    public UserSpeechHistoryList() {
        this.histories = new ArrayList<>();
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
                vo.setItems(histories.get(half).getItems());
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


    @Data
    @ToString
    static class UserSpeechHistory implements Serializable {
        private Integer mid;
        private List<Reply> items;
        private static final Set<Integer> KEYS = new HashSet<>();

        public UserSpeechHistory(Integer mid) {
            this.mid = mid;
            this.items = new ArrayList<>();
            KEYS.add(mid);
        }

        public void add(Reply reply) {
            if (items.stream().filter(f -> f.getCtime() == reply.getCtime()).count() <= 0) {
                items.add(reply);
                items.sort(Comparator.comparing(Reply::getCtime));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserSpeechHistory that = (UserSpeechHistory) o;
            return Objects.equals(mid, that.mid) && Objects.equals(items, that.items);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mid, items);
        }
    }
}
