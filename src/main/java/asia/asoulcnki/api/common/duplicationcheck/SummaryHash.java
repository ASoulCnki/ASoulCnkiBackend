package asia.asoulcnki.api.common.duplicationcheck;

import java.math.BigInteger;
import java.util.ArrayList;

public class SummaryHash {

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
		ArrayList<Long> hashPicks = new ArrayList<>();
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

	private static long pickHash(ArrayList<Long> hashVals, int start, int end) {
		int index = start;
		long minHash = Long.MAX_VALUE;
		while (index < end) {
			if (hashVals.get(index) < minHash) {
				minHash = hashVals.get(index);
			}
			index += 1;
		}
		return minHash;
	}

	public static ArrayList<Long> defaultHash(String s) {
		return hash(s, 4, 1145141919780L, 2, 4);
	}

	public static void main(String[] args) {
		String s = "我是皇家🤡团中的一员，你也可以称我为音乐珈";
		ArrayList<Long> result = hash(s, 4, 1145141919780L, 2, 4);
		System.out.println(result);
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
