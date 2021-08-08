package asia.asoulcnki.api.common.duplicationcheck;

import asia.asoulcnki.api.common.duplicationcheck.filters.KeywordsFilter;
import asia.asoulcnki.api.common.duplicationcheck.filters.UserIdInFilter;
import asia.asoulcnki.api.persistence.entity.Reply;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class FilterRulesContainer {

	public Set<Predicate<Reply>> predicates = new HashSet<>();


	public void addUserIDInFilter(List<Integer> userIDs) {
		predicates.add(new UserIdInFilter(userIDs));
	}

	public Predicate<Reply> getFilter() {
		return predicates.stream().reduce(CommonFilterRules.alwaysTrue(), Predicate::and);
	}

	public void addContainsKeywordsPredicate(List<String> keywords) {
		predicates.add(new KeywordsFilter(keywords));
	}

	@Override
	public int hashCode() {
		return Objects.hash(predicates);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final FilterRulesContainer that = (FilterRulesContainer) o;
		return Objects.equals(predicates, that.predicates);
	}
}
