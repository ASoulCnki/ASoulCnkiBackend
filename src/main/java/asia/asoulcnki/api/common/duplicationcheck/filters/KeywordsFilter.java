package asia.asoulcnki.api.common.duplicationcheck.filters;

import asia.asoulcnki.api.persistence.entity.Reply;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class KeywordsFilter implements Predicate<Reply> {

	private static final List<String> emptyKeywords = Collections.emptyList();

	private final List<String> keywords;

	public KeywordsFilter(final List<String> keywords) {
		if (isAllBlank(keywords)) {
			this.keywords = emptyKeywords;
			return;
		}
		this.keywords = keywords;
	}

	private static boolean isAllBlank(final List<String> keywords) {
		return keywords == null || keywords.stream().allMatch(StringUtils::isBlank);
	}

	@Override
	public boolean test(final Reply reply) {
		for (String keyword : keywords) {
			if (StringUtils.isBlank(keyword)) {
				continue;
			}
			if (!isContainsChinese(reply.getContent(), keyword)) {
				return false;
			}
		}
		return true;
	}

	public boolean isContainsChinese(String str1, String str2) {
		String textviewString = ((CharSequence) str1).toString();
		return textviewString.contains(str2);
	}

	@Override
	public int hashCode() {
		return Objects.hash(keywords);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final KeywordsFilter that = (KeywordsFilter) o;
		return Objects.equals(keywords, that.keywords);
	}
}
