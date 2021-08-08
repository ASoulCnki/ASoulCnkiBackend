package asia.asoulcnki.api.common.duplicationcheck.filters;

import asia.asoulcnki.api.persistence.entity.Reply;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class UserIdInFilter implements Predicate<Reply> {

	private final List<Integer> userIDs;

	public UserIdInFilter(final List<Integer> userIDs) {
		this.userIDs = userIDs;
	}

	@Override
	public boolean test(final Reply reply) {
		if (userIDs == null || userIDs.isEmpty()) {
			return true;
		}
		for (Integer id : userIDs) {
			if (id != null && reply.getUid() == id) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(userIDs);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final UserIdInFilter that = (UserIdInFilter) o;
		return Objects.equals(userIDs, that.userIDs);
	}
}
