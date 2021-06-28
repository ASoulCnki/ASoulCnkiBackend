package asia.asoulcnki.api.persistence.service.impl;

import asia.asoulcnki.api.common.duplicationcheck.ComparisonDatabase;
import asia.asoulcnki.api.common.duplicationcheck.SummaryHash;
import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.service.ICheckService;
import asia.asoulcnki.api.persistence.vo.CheckResultVo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CheckServiceImpl implements ICheckService {
	@Override
	public ApiResult<CheckResultVo> check(final String text) {
		ArrayList<Long> textHashList = SummaryHash.defaultHash(text);
		Map<Long, Integer> replyHitMap = new HashMap<>();
		int codePointCount = text.codePointCount(0, text.length());
		if (codePointCount > 1000) {
			return ApiResult.error(400, "text too long");
		}

		ComparisonDatabase db = ComparisonDatabase.getInstance();

		for (Long textHash : textHashList) {
			ArrayList<Long> hitReplyIds = db.searchHash(textHash);
			if (hitReplyIds != null) {
				for (long id : hitReplyIds) {
					if (replyHitMap.containsKey(id)) {
						int count = replyHitMap.get(id);
						replyHitMap.put(id, count + 1);
					} else {
						replyHitMap.put(id, 1);
					}
				}
			}
		}

		List<List<Object>> related = new ArrayList<>(10);

		float threshHold = (float) ((float) textHashList.size() * 0.2);
		Comparator<Map.Entry<Long, Integer>> cmp = Map.Entry.comparingByValue();
		List<Map.Entry<Long, Integer>> sortedList =
				replyHitMap.entrySet().stream().filter(entry -> entry.getValue() > threshHold).sorted(cmp.reversed()).collect(Collectors.toList());

		StringBuilder allContentBuilder = new StringBuilder();

		for (Map.Entry<Long, Integer> entry : sortedList) {
			Reply reply = db.getReply(entry.getKey());
			String content = reply.getContent();
			allContentBuilder.append(content);
			float similarity = SummaryHash.compareArticle(text, content);
			String replyUrl = getReplyUrl(reply);
			related.add(Lists.newArrayList(similarity, reply, replyUrl));
		}

		related.sort((lhs, rhs) -> {
			float lhsSimilarity = (float) lhs.get(0);
			float rhsSimilarity = (float) rhs.get(0);

			if (lhsSimilarity != rhsSimilarity) {
				return (int) (lhsSimilarity - rhsSimilarity);
			}

			int lhsCTime = ((Reply) lhs.get(1)).getCtime();
			int rhsCTime = ((Reply) rhs.get(1)).getCtime();

			return lhsCTime - rhsCTime;

		});

		float allSimilarity = 0;
		String allContent = allContentBuilder.toString();
		if (!StringUtils.isBlank(allContent)) {
			allSimilarity = SummaryHash.compareArticle(text, allContentBuilder.toString());
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

		return ApiResult.ok(vo);
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
