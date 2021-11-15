package asia.asoulcnki.api.common.duplicationcheck;

import java.math.BigInteger;
import java.util.ArrayList;

public class SummaryHash {

    /**
     * 取样采取连续k个字符进行hash，即连续k个字符相同才判断为相同
     */
	public static final int DEFAULT_K = 8;
    /**
     * 默认的取样滑动窗口，即每个窗口宽度，每个窗口取最小值作为特征hash
     */
	public static final int DEFAULT_W = 8;
    /**
     * hash取模的模数，影响碰撞几率
     */
	private static final long DEFAULT_Q = 100001651L;
    /**
     * hash基数，为质数能减少碰撞
     */
	private static final int DEFAULT_B = 2;


    /**
     * 获取一个取样窗口(k个字符)字符串的hash值
     * @param s 字符串
     * @param nextStartOffset 下次取样开始下标
     * @param k 敏感度 每n个字做摘要
     * @param q 模数 摘要算法结果对其取模
     * @param b 基数
     * @return (此次取样hash值, 下次取样开始下标) Tuple<>
     */
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
     * 获取经过滑动窗口筛选的字符串hash结果
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

    /**
     * 获取未经筛选的字符串hash结果
     * @param s 字符串
     * @param k 敏感度 每n个字做摘要
     * @param q 模数 摘要算法结果对其取模
     * @param b 基数
     * @return 未经过筛选的字符串的摘要 ArrayList
     */
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

    /**
     * 在一个滑动窗口中选择合适的hash
     * @param hashVals 待筛选的hash结果
     * @param start 滑动窗口起始下标(index)
     * @param end 滑动窗口结束下标(index+w)
     * @return 选择出的hash值 long
     */
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

    /**
     * 使用默认参数取字符串s的hash
     * @param s 待取hash的字符串
     * @return hash结果 ArrayList
     */
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
		System.out.println(ArticleCompareUtil.trim(text3));
		ArrayList<Long> result1 = defaultHash(text1);
		System.out.println(result1);
		ArrayList<Long> result2 = defaultHash(text2);
		System.out.println(result2);
		System.out.println(ArticleCompareUtil.compareArticle(text1, text2));
		System.out.println(ArticleCompareUtil.compareArticle(text2, text2));
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
