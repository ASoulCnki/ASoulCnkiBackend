package asia.asoulcnki.api.service.impl;

import asia.asoulcnki.api.common.duplicationcheck.LeaderBoard;
import asia.asoulcnki.api.common.duplicationcheck.LeaderBoard.LeaderBoardEntry;
import asia.asoulcnki.api.persistence.vo.RankingResultVo;
import asia.asoulcnki.api.service.IRankingService;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "checkCache")
public class IRankingServiceImpl implements IRankingService {

	@Override
	@Cacheable(value = "leaderboard")
	public RankingResultVo queryRankings(final SortMethodEnum sortMethod, final TimeRangeEnum timeRange,
			final List<Integer> userIDs ,final int pageSize, final int pageNum) {
		LeaderBoardEntry leaderBoard;

		switch (sortMethod) {
		case SIMILAR_COUNT:
			leaderBoard = LeaderBoard.getInstance(userIDs).getSimilarCountLeaderBoard();
			break;
		case LIKE_NUM:
			leaderBoard = LeaderBoard.getInstance(userIDs).getLikeLeaderBoard();
			break;
		default:
			leaderBoard = LeaderBoard.getInstance(userIDs).getSimilarLikeSumLeaderboard();
		}

		return leaderBoard.query(timeRange ,pageSize, pageNum);
	}

	@Override
	@CacheEvict(value = "leaderboard", allEntries = true)
	public void refresh() {
		LeaderBoard.getInstance(null).refresh();
	}
}
