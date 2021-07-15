package asia.asoulcnki.api.common.duplicationcheck;

import com.google.common.collect.Lists;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SummaryHash {

	public static final int DEFAULT_K = 6;
	public static final int DEFAULT_W = 6;
	private static final long DEFAULT_Q = 100001651L;
	private static final int DEFAULT_B = 2;


	private static Tuple<Long, Integer> hashValue(String s, int nextStartOffset, int k, long q, int b) {
		BigInteger hash = BigInteger.valueOf(0);
		int offset = nextStartOffset;
		int index = 0;
		while (index < k) {
			// get unicode code point
			final int codepoint = s.codePointAt(offset);
			// calculate hash, codePoint * (b ^ (k - 1 - index))
			BigInteger rhs = BigInteger.valueOf(b).pow(k - 1 - index);
			hash = hash.add(BigInteger.valueOf(codepoint).multiply(rhs));

			offset += Character.charCount(codepoint);
			index++;
			if (index == 1) {
				nextStartOffset = offset;
			}
		}
		return new Tuple<>(hash.longValue() % q, nextStartOffset);
	}

	/**
	 * @param s 字符串
	 * @param k 敏感度 每n个字做摘要
	 * @param q 模数 摘要算法结果对其取模
	 * @param b 基数
	 * @param w 窗口宽度 摘要时的窗口大小
	 * @return 字符串的摘要
	 */

	public static ArrayList<Long> hash(String s, int k, long q, int b, int w) {
		ArrayList<Long> hashVals = getStringHashVals(s, k, q, b);

		ArrayList<Long> hashPicks = new ArrayList<>(hashVals.size());
		hashPicks.add(pickHash(hashVals, 0, w));
		int j = 1;
		while (j + w <= hashVals.size()) {
			long minHash = pickHash(hashVals, j, j + w);
			if (minHash != hashPicks.get(hashPicks.size() - 1)) {
				hashPicks.add(minHash);
			}
			j += 1;
		}
		return hashPicks;
	}

	public static ArrayList<Long> getStringHashVals(String s, int k, long q, int b) {
		int codePointNumber = s.codePointCount(0, s.length());
		int nextOffset = 0;
		ArrayList<Long> hashVals = new ArrayList<>(codePointNumber - k + 1);
		// i indicate the i-th unicode code point
		// for example 我是🤡 , 我 is the first and 🤡 is the third
		for (int i = 0; i < codePointNumber - k + 1; i++) {
			Tuple<Long, Integer> result = hashValue(s, nextOffset, k, q, b);
			long hashVal = result.first;
			nextOffset = result.second;
			hashVals.add(hashVal);
		}
		return hashVals;
	}

	// start = index, end = index + w
	private static long pickHash(ArrayList<Long> hashVals, int start, int end) {
		int index = start;
		long minHash = Long.MAX_VALUE;
		while (index < end && index < hashVals.size()) {
			if (hashVals.get(index) < minHash) {
				minHash = hashVals.get(index);
			}
			index += 1;
		}
		return minHash;
	}

	public static float compareArticle(String article1, String article2) {
		int codePointsCount = article1.codePointCount(0, article1.length());
		List<String> article1Segs = getStringSegs(article1);
		Set<String> article2Segs = new HashSet<>(getStringSegs(article2));
		float count = 0;
		Set<Integer> redList = new HashSet<>();
		for (int i = 0; i < codePointsCount - DEFAULT_K + 1; i++) {
			String seg = article1Segs.get(i);
			if (article2Segs.contains(seg)) {
				for (int j = 0; j < DEFAULT_K; j++) {
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

	// TODO w 的 长度 和 k 的长度
	// 找评论区里表现不太好的查重结果
	// 去掉空格和回车，标点符号之后再计算hash值
	public static ArrayList<Long> defaultHash(String s) {
		return hash(s, DEFAULT_K, DEFAULT_Q, DEFAULT_B, DEFAULT_W);
	}

	public static void main(String[] args) {
		String text1 =
				"嘉然的脚小小的香香的，不像手经常使用来得灵活，但有一种独特的可爱的笨拙，嫩嫩的脚丫光滑细腻，凌莹剔透，看得见皮肤下面细细的血管与指甲之下粉白的月牙。再高冷的女生小脚也是敏感的害羞的，轻轻挠一挠，她就摇身一变成为娇滴滴的女孩，脚丫像是一把钥匙，轻轻掌握它就能打开女孩子的心灵。";
		String text2 = "嘉然的脚小小的香香的,不像手经常使用来得灵活,但有一种独特的可爱的笨拙,嫩嫩的脚丫光滑细腻，凌莹剔透," +
				"看得见皮肤下面细细的血管与指甲之下粉白的月牙再高冷的女生小脚也是敏感的害羞的，轻轻挠一挠，她就摇身一变成为娇滴滴的女孩,脚丫像是一把钥匙，轻轻掌握它就能打开女孩子的心灵。";
		String text3 = "乃琳带我走吧！            双向奔赴～\n" + "                                         \n" + "               "
				+ "                       \n" + "＼\uD83E\uDD24／                        ＼\uD83E\uDD8A／        　\n" + " "
				+ "  " + "  /                                    \\  \n" + "ノ)                                     (㇏";
		System.out.println(trim(text3));
		ArrayList<Long> result1 = defaultHash(text1);
		System.out.println(result1);
		ArrayList<Long> result2 = defaultHash(text2);
		System.out.println(result2);
		System.out.println(compareArticle(text1, text2));
		System.out.println(compareArticle(text2, text2));
	}

	public static class Tuple<A, B> {
		public final A first;
		public final B second;

		public Tuple(A a, B b) {
			first = a;
			second = b;
		}
	}
}
