package asia.asoulcnki.api.service.impl;

import asia.asoulcnki.api.common.duplicationcheck.ComparisonDatabase;
import asia.asoulcnki.api.common.duplicationcheck.LeaderBoard;
import asia.asoulcnki.api.common.duplicationcheck.LeaderBoard.LeaderBoardEntry;
import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.vo.RankingResultVo;
import asia.asoulcnki.api.service.IRankingService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

@Service
@CacheConfig(cacheNames = "caffeineCacheManager")
public class IRankingServiceImpl implements IRankingService {


	public List<Reply> queryLeaderBoard(LeaderBoardEntry leaderBoard, Predicate<Reply> predicate, int pageSize,
			int pageNum) {
		return leaderBoard.query(predicate, pageSize, pageNum);
	}

	@Override
	@Cacheable(value = "leaderboard")
	public RankingResultVo queryRankings(final SortMethodEnum sortMethod, final TimeRangeEnum timeRange,
			final int pageSize, final int pageNum) {
		LeaderBoardEntry leaderBoard;

		switch (sortMethod) {
		case SIMILAR_COUNT:
			leaderBoard = LeaderBoard.getInstance().getSimilarCountLeaderBoard();
			break;
		case LIKE_NUM:
			leaderBoard = LeaderBoard.getInstance().getLikeLeaderBoard();
			break;
		default:
			leaderBoard = LeaderBoard.getInstance().getLikeSummaryLeaderBoard();
		}

		// set time filter
		Predicate<Reply> predicate = r -> true;
		Calendar c = Calendar.getInstance();
		Date date = new Date(ComparisonDatabase.getInstance().getMaxTime() * 1000L);
		c.setTime(date);
		switch (timeRange) {
		case ONE_WEEK:
			c.add(Calendar.DAY_OF_WEEK, -1);
			predicate = r -> r.getCtime() * 1000L >= c.getTime().getTime();
			break;
		case THREE_DAYS:
			c.add(Calendar.DATE, -3);
			predicate = r -> r.getCtime() * 1000L >= c.getTime().getTime();
			break;
		}

		List<Reply> result = queryLeaderBoard(leaderBoard, predicate, pageSize, pageNum);

		return new RankingResultVo(result);
	}

	@Override
	@CacheEvict(value = "leaderboard")
	public void refresh() {
		LeaderBoard.getInstance().refresh();
	}
}
