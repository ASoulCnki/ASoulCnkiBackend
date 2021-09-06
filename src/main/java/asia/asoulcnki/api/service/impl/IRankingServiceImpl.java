package asia.asoulcnki.api.service.impl;

import asia.asoulcnki.api.common.duplicationcheck.FilterRulesContainer;
import asia.asoulcnki.api.common.duplicationcheck.LeaderBoard;
import asia.asoulcnki.api.common.duplicationcheck.LeaderBoard.LeaderBoardEntry;
import asia.asoulcnki.api.persistence.vo.RankingResultVo;
import asia.asoulcnki.api.service.IRankingService;
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
                                         final FilterRulesContainer container, final int pageSize, final int pageNum) {
        LeaderBoardEntry leaderBoard;

        switch (sortMethod) {
            case SIMILAR_COUNT:
                leaderBoard = LeaderBoard.getInstance().getSimilarCountLeaderBoard();
                break;
            case LIKE_NUM:
                leaderBoard = LeaderBoard.getInstance().getLikeLeaderBoard();
                break;
            default:
                leaderBoard = LeaderBoard.getInstance().getSimilarLikeSumLeaderboard();
        }

        return leaderBoard.query(container.getFilter(), timeRange, pageSize, pageNum);
    }

    @Override
    @CacheEvict(value = "leaderboard", allEntries = true)
    public void refresh() {
        LeaderBoard.getInstance().refresh();
    }
}
