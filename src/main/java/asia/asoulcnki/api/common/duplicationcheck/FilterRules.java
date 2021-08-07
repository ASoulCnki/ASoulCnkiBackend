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
		return reply -> {
			// default condition
			if (userIDs == null || userIDs.isEmpty()) {
				return true;
			}

			for (int id : userIDs) {
				if (reply.getUid() == id) {
					return true;
				}
			}
			return false;
		};
	}
}
