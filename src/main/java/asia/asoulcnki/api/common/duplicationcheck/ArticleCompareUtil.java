package asia.asoulcnki.api.common.duplicationcheck;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 小作文比对工具类
 */
public class ArticleCompareUtil {
    /**
     * 判断小作文是否高度相似
     *
     * @param textLength 小作文长度
     * @param similarity 相似度
     * @return
     */
    public static boolean isHighSimilarity(int textLength, float similarity) {
        if (textLength > 250) {
            return similarity > 0.5;
        } else if (textLength > 150) {
            return similarity > 0.6;
        } else {
            return similarity > 0.7;
        }
    }

    /**
     * 过滤空白字符、标点符号、零宽空格等特殊字符
     *
     * @param s 小作文
     * @return 经过过滤后的小作文
     */
    public static String trim(String s) {
        String stopWord = "[\\pP\\p{Punct}]";
        s = s.replaceAll("\\s*", "");
        //去除控制字符
        s = s.replaceAll("\\p{Cf}", "");
        //去除零宽空格等
        s = s.replaceAll("/[\\u200b-\\u200f\\ufeff\\u202a-\\u202e]/g", "");
        s = s.replaceAll(stopWord, "");
        return s;
    }

    /**
     * 使用滑动窗口将字符串分段，以便 compareArticle 方法比较
     *
     * @param s 小作文
     * @return
     */
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

    /**
     * 获取两篇小作文的文本重复度
     *
     * @param article1
     * @param article2
     * @return 文本重复率
     */
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

    /**
     * 获取小作文字数
     *
     * @param s 小作文字符串
     * @return 作文字数
     */
    public static int textLength(String s) {
        if (StringUtils.isBlank(s)) {
            return 0;
        }
        return s.codePointCount(0, s.length());
    }
}
