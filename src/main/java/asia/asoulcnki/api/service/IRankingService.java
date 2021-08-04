package asia.asoulcnki.api.service;

import java.util.List;

import asia.asoulcnki.api.persistence.vo.RankingResultVo;

public interface IRankingService {
	RankingResultVo queryRankings(SortMethodEnum sortMethod, final TimeRangeEnum timeRange, List<Integer> userIDs,
            final int pageSize, final int pageNum);

	void refresh();

	enum SortMethodEnum {
		DEFAULT, LIKE_NUM, SIMILAR_COUNT
	}

	enum TimeRangeEnum {
		ALL, ONE_WEEK, THREE_DAYS
	}
}
