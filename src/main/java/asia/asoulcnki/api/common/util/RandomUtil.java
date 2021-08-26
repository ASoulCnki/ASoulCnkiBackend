package asia.asoulcnki.api.common.util;

import java.util.*;

public class RandomUtil {
    public static <T> T choice(List<T> list) {
        Random random = new Random();
        int n = random.nextInt(list.size());
        return list.get(n);
    }

    public static <T> List<T> sample(List<T> list, int num) {
        if (list.size() < num) {
            return list;
        }
        Random random = new Random();
        Map<Integer, T> map = new HashMap<>();
        while (map.size() < num) {
            int n = random.nextInt(list.size());
            if (!map.containsKey(n)) {
                T data = list.get(n);
                map.put(n, data);
            }
        }
        List<T> ret = new ArrayList<>(map.values());
        Collections.shuffle(ret);
        return ret;
    }
}
