package asia.asoulcnki.api.persistence.service.impl;

import asia.asoulcnki.api.common.BizException;
import asia.asoulcnki.api.common.duplicationcheck.ArticleCompareUtil;
import static asia.asoulcnki.api.common.duplicationcheck.ArticleCompareUtil.isHighSimilarity;
import asia.asoulcnki.api.common.duplicationcheck.ComparisonDatabase;
import asia.asoulcnki.api.common.duplicationcheck.SummaryHash;
import asia.asoulcnki.api.common.response.CnkiCommonEnum;
import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.service.ICheckService;
import asia.asoulcnki.api.persistence.vo.CheckResultVo;
import asia.asoulcnki.api.persistence.vo.CheckResultVo.RelatedReply;
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
@CacheConfig(cacheNames = "caffeineCacheManager")
public class CheckServiceImpl implements ICheckService {
	private final static Logger log = LoggerFactory.getLogger(CheckServiceImpl.class);


	@Override
	public CheckResultVo check(String text) {
		text = ArticleCompareUtil.trim(text);
		int textLength = text.codePointCount(0, text.length());
		if (textLength < SummaryHash.DEFAULT_K) {
			log.error("the text to check is too short, codepoint number {} ", textLength);
			throw new BizException(CnkiCommonEnum.TEXT_TO_CHECK_TOO_SHORT);
		} else if (textLength > 2000) {
			log.error("the text to check is too long, codepoint number {} ", textLength);
			throw new BizException(CnkiCommonEnum.TEXT_TO_CHECK_TOO_LONG);
		}
		return getDuplicationCheckResult(text);
	}

	@Cacheable(key = "#text", value = "replyCache")
	public CheckResultVo getDuplicationCheckResult(String text) {
		ComparisonDatabase db = ComparisonDatabase.getInstance();

		ArrayList<Long> textHashList = SummaryHash.defaultHash(text);

		int textLength = text.codePointCount(0, text.length());
		int minHit = 1;
		if (textLength > 30) {
			minHit = 2;
		}
		List<Map.Entry<Long, Integer>> sortedRelatedReplyIdList = ComparisonDatabase.searchRelatedReplies(textHashList
				, minHit);
		List<RelatedReply> related = new ArrayList<>(textHashList.size() / 2);

		StringBuilder allContentBuilder = new StringBuilder();

		for (Map.Entry<Long, Integer> entry : sortedRelatedReplyIdList) {
			Reply reply = db.getReply(entry.getKey());
			String content = ArticleCompareUtil.trim(reply.getContent());
			float similarity = ArticleCompareUtil.compareArticle(text, content);
			if (similarity < 0.2) {
				continue;
			}
			String replyUrl = getReplyUrl(reply);
			allContentBuilder.append(content);
			related.add(new RelatedReply(similarity, reply, replyUrl));
		}

		// param -> left hand side and right hand side
		related.sort((lhs, rhs) -> {
			float lhsSimilarity = lhs.getRate();
			int lhsCTime = lhs.getReply().getCtime();

			float rhsSimilarity = rhs.getRate();
			int rhsCTime = rhs.getReply().getCtime();

			if (isHighSimilarity(textLength, lhsSimilarity) && isHighSimilarity(textLength, rhsSimilarity)) {
				return Integer.compare(lhsCTime, rhsCTime);
			}

			if (lhsSimilarity != rhsSimilarity) {
				return -Float.compare(lhsSimilarity, rhsSimilarity);
			} else {
				return Integer.compare(lhsCTime, rhsCTime);
			}
		});

		float allSimilarity = 0;
		String allContent = allContentBuilder.toString();
		if (!StringUtils.isBlank(allContent)) {
			allSimilarity = ArticleCompareUtil.compareArticle(text, allContentBuilder.toString());
		}


		CheckResultVo vo = new CheckResultVo();
		vo.setStartTime(db.getMinTime());
		vo.setEndTime(db.getMaxTime());
		vo.setAllSimilarity(allSimilarity);
		if (related.size() > 5) {
			vo.setRelated(related.subList(0, 5));
		} else {
			vo.setRelated(related);
		}
		return vo;
	}

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
