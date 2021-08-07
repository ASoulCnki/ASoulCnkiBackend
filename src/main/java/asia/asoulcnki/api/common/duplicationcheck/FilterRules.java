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
		Predicate<Reply> userPredicate = new Predicate<Reply>() {
            @Override
            public boolean test(Reply reply) {
                if (userIDs == null) {
                    return true;
                }

                for (int id : userIDs) {
                    if (reply.getMid() == id) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public int hashCode() {
                int hash = 0;
                if (userIDs == null) {
                    return hash;
                }

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
