package asia.asoulcnki.api.common.duplicationcheck;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArticleCompareUtil {
	public static boolean isHighSimilarity(int textLength, float similarity) {
		if (textLength > 250) {
			return similarity > 0.5;
		} else if (textLength > 150) {
			return similarity > 0.6;
		} else {
			return similarity > 0.7;
		}
	}

	public static String trim(String s) {
		String stopWord = "[\\pP\\p{Punct}]";
		s = s.replaceAll("\\s*", "");
		s = s.replaceAll(stopWord, "");
		return s;
	}

	static List<String> getStringSegs(String s) {
		int codePointCount = s.codePointCount(0, s.length());
		if (codePointCount <= SummaryHash.DEFAULT_K) {
			return Lists.newArrayList(s);
		}
		int startOffset = 0;
		List<String> stringSegs = new ArrayList<>(codePointCount - SummaryHash.DEFAULT_K + 1);
		for (int i = 0; i < codePointCount - SummaryHash.DEFAULT_K + 1; i++) {
			String subString = unicodeSubString(s, startOffset, SummaryHash.DEFAULT_K);
			startOffset = s.offsetByCodePoints(startOffset, 1);
			stringSegs.add(subString);
		}
		return stringSegs;
	}

	static String unicodeSubString(String str, int idx, int len) {
		return str.substring(idx, str.offsetByCodePoints(idx, len));
	}

	public static float compareArticle(String article1, String article2) {
		int codePointsCount = article1.codePointCount(0, article1.length());
		List<String> article1Segs = getStringSegs(article1);
		Set<String> article2Segs = new HashSet<>(getStringSegs(article2));
		float count = 0;
		Set<Integer> redList = new HashSet<>();
		for (int i = 0; i < codePointsCount - SummaryHash.DEFAULT_K + 1; i++) {
			String seg = article1Segs.get(i);
			if (article2Segs.contains(seg)) {
				for (int j = 0; j < SummaryHash.DEFAULT_K; j++) {
					redList.add(i + j);
				}
			}
		}
		for (int i = 0; i < codePointsCount; i++) {
			if (redList.contains(i)) {
				count += 1;
			}
		}

		return count / (float) codePointsCount;
	}

	public static int textLength(String s) {
		if (StringUtils.isBlank(s)) {
			return 0;
		}
		return s.codePointCount(0, s.length());
	}

	public static void main(String[] args) {
		String s = "7月9日，认识了一个男人，他跟我聊叔本华，聊弗洛伊，聊庄子妻死，聊伽罗瓦和近世代数，聊彭罗斯和宇宙督察假说\n" + "\n" + "7月10日，我让他b站关注嘉然今天吃什么";
		System.out.println(textLength(trim(s)));
	}
}
