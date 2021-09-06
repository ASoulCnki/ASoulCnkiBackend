package asia.asoulcnki.api.service.impl;

import asia.asoulcnki.api.common.BizException;
import asia.asoulcnki.api.common.duplicationcheck.ArticleCompareUtil;

import static asia.asoulcnki.api.common.duplicationcheck.ArticleCompareUtil.isHighSimilarity;

import asia.asoulcnki.api.common.duplicationcheck.ComparisonDatabase;
import asia.asoulcnki.api.common.duplicationcheck.SummaryHash;
import asia.asoulcnki.api.common.response.CnkiCommonEnum;
import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.vo.CheckResultVo;
import asia.asoulcnki.api.persistence.vo.CheckResultVo.RelatedReply;
import asia.asoulcnki.api.service.ICheckService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames = "checkCache")
public class ICheckServiceImpl implements ICheckService {
    private final static Logger log = LoggerFactory.getLogger(ICheckServiceImpl.class);
    /**
     * 小作文最大长度限制
     */
    private final static int TEXT_MAX_LENGTH = 1000;

    /**
     * 调高 minHit 的文本长度阈值
     */
    private final static int INCREASE_MIN_HIT_THRESHOLD = 30;

    /**
     * 若相似小作文的个数超过 RELATED_ARTICLE_NUM，则只展示前 RELATED_ARTICLE_NUM 个
     */
    private final static int RELATED_ARTICLE_NUM = 5;

    /**
     * 判断小作文相似的阈值
     */
    private static final double SIMILARITY_THRESHOLD = 0.2;

    @Override
    public CheckResultVo check(String text) {
        // 先过滤空白字符、换行符等
        text = ArticleCompareUtil.trim(text);
        int textLength = text.codePointCount(0, text.length());
        // 小作文不能太短或者太长
        if (textLength < SummaryHash.DEFAULT_K) {
            log.error("the text to check is too short, codepoint number {} ", textLength);
            throw new BizException(CnkiCommonEnum.TEXT_TO_CHECK_TOO_SHORT);
        } else if (textLength > TEXT_MAX_LENGTH) {
            log.error("the text to check is too long, codepoint number {} ", textLength);
            throw new BizException(CnkiCommonEnum.TEXT_TO_CHECK_TOO_LONG);
        }
        return getDuplicationCheckResult(text);
    }

    /**
     * 在对比库中查找与 text 相似的小作文
     *
     * @param text 待查重文本
     * @return 查重结果 CheckResultVO
     */
    @Cacheable(key = "#text", value = "replyCache")
    public CheckResultVo getDuplicationCheckResult(String text) {
        ComparisonDatabase db = ComparisonDatabase.getInstance();

        ArrayList<Long> textHashList = SummaryHash.defaultHash(text);

        int textLength = text.codePointCount(0, text.length());
        // minHit 至少查询命中的 hash 次数
        int minHit = 1;
        // 如果小作文长度长过阈值，则调高 minHit
        if (textLength > INCREASE_MIN_HIT_THRESHOLD) {
            minHit = 2;
        }
        // sortedRelatedReplayIdList 是所有包含 hash 值的评论的 <rid（评论id),hitCount(hash 命中次数)>键值对列表
        List<Map.Entry<Long, Integer>> sortedRelatedReplyIdList = ComparisonDatabase.searchRelatedReplies(textHashList
            , minHit);
        // related 是相似小作文列表
        List<RelatedReply> related = new ArrayList<>(textHashList.size() / 2);

        StringBuilder allContentBuilder = new StringBuilder();

        // 遍历列表，将输入与列表元素逐一比对，看是否有相似小作文
        for (Map.Entry<Long, Integer> entry : sortedRelatedReplyIdList) {
            Reply reply = db.getReply(entry.getKey());
            String content = ArticleCompareUtil.trim(reply.getContent());
            // 预处理后如果相似度低于阈值，则认为不相似
            float similarity = ArticleCompareUtil.compareArticle(text, content);
            if (similarity < SIMILARITY_THRESHOLD) {
                continue;
            }
            String replyUrl = getReplyUrl(reply);
            allContentBuilder.append(content);
            related.add(new RelatedReply(similarity, reply, replyUrl));
        }

        // 对查到的相似小作文进行排序
        related.sort((leftHandSide, rightHandSide) -> {
            float lhsSimilarity = leftHandSide.getRate();
            int lhsCTime = leftHandSide.getReply().getCtime();

            float rhsSimilarity = rightHandSide.getRate();
            int rhsCTime = rightHandSide.getReply().getCtime();

            // 如果都是高相似小作文，则优先根据评论时间先后排序
            if (isHighSimilarity(textLength, lhsSimilarity) && isHighSimilarity(textLength, rhsSimilarity)) {
                return Integer.compare(lhsCTime, rhsCTime);
            }

            // 如果相似的小作文都不是高度相似，则优先根据相似度高低排序，相似度相同再根据评论时间先后排序
            if (Float.compare(lhsSimilarity, rhsSimilarity) != 0) {
                return -Float.compare(lhsSimilarity, rhsSimilarity);
            } else {
                return Integer.compare(lhsCTime, rhsCTime);
            }
        });

        // 计算总复制比
        float allSimilarity = 0;
        String allContent = allContentBuilder.toString();
        if (!StringUtils.isBlank(allContent)) {
            allSimilarity = ArticleCompareUtil.compareArticle(text, allContentBuilder.toString());
        }

        // 构建查重结果VO
        CheckResultVo vo = new CheckResultVo();
        vo.setStartTime(db.getMinTime());
        vo.setEndTime(db.getMaxTime());
        vo.setAllSimilarity(allSimilarity);
        // 若相似小作文的个数超过 RELATED_ARTICLE_NUM，则只展示前 RELATED_ARTICLE_NUM 个
        if (related.size() > RELATED_ARTICLE_NUM) {
            vo.setRelated(related.subList(0, RELATED_ARTICLE_NUM));
        } else {
            vo.setRelated(related);
        }
        return vo;
    }

    /**
     * 获取原评论链接
     *
     * @param reply Reply 对象
     * @return 原评论 URL 链接
     */
    private String getReplyUrl(Reply reply) {
        String baseUrl = "https://www.bilibili.com";
        String dynamicBaseUrl = "https://t.bilibili.com";
        switch (reply.getTypeId()) {
            case 1:
                return String.format(" %s/video/av%d/#reply%d", baseUrl, reply.getOid(), reply.getRpid());
            case 11:
            case 17:
                return String.format(" %s/%d/#reply%d", dynamicBaseUrl, reply.getDynamicId(), reply.getRpid());
            case 12:
                return String.format(" %s/read/cv%d/#reply%d", baseUrl, reply.getOid(), reply.getRpid());
            default:
                return "";
        }
    }
}
