package asia.asoulcnki.api.common.duplicationcheck;

import asia.asoulcnki.api.persistence.entity.Reply;

import java.util.List;
import java.util.function.Predicate;

public class FilterRules {
	public static Predicate<Reply> predicate;

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

	public static Predicate<Reply> userIDIn(List<Integer> userIDs) {
        if (userIDs == null || userIDs.isEmpty()) {
            return null;
        }
		Predicate<Reply> userPredicate = new Predicate<Reply>() {
            @Override
            public boolean test(Reply reply) {
                for (int id : userIDs) {
                    if (reply.getUid() == id) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public int hashCode() {
                int hash = 0;
                for (int id : userIDs) {
                    hash += id;
                }
                return hash;
            }

            @Override
            public boolean equals(Object o) {
                return this.hashCode() == o.hashCode();
            }
        };
        return userPredicate;
	}
}
