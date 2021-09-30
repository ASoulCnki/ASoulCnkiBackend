package asia.asoulcnki.api.common.duplicationcheck;

import asia.asoulcnki.api.persistence.entity.Reply;

import java.util.function.Predicate;

public class CommonFilterRules {
    private CommonFilterRules() {
    }

    public static Predicate<Reply> similarLikeSumGreaterThan(int threshold) {
        return r -> r.getSimilarLikeSum() > threshold;
    }

    public static Predicate<Reply> likeNumGreaterThan(int threshold) {
        return r -> r.getLikeNum() > threshold;
    }

    public static Predicate<Reply> similarLikeCountGreaterThan(int threshold) {
        return r -> r.getSimilarCount() > threshold;
    }

    public static Predicate<Reply> alwaysTrue() {
        return r -> true;
    }
}
