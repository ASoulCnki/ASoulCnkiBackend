package asia.asoulcnki.api.common.duplicationcheck;

import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.vo.RankingResultVo;
import asia.asoulcnki.api.service.IRankingService.TimeRangeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LeaderBoard {

    private final static Logger log = LoggerFactory.getLogger(LeaderBoard.class);

    private static LeaderBoard instance;
    /**
     * 累积赞数排序，至少为50，且引用次数至少为1
     */
    private final LeaderBoardEntry similarLikeSumLeaderboard;
    /**
     * 单评论赞数排序，至少为50
     */
    private final LeaderBoardEntry likeLeaderBoard;
    /**
     * 引用次数排序，至少为5
     */
    private final LeaderBoardEntry similarCountLeaderBoard;

    private LeaderBoard() {

        similarLikeSumLeaderboard = new LeaderBoardEntry(Comparator.comparing(Reply::getSimilarLikeSum).reversed());
        similarLikeSumLeaderboard.setAllRepliesFilter(CommonFilterRules.similarLikeSumGreaterThan(50).and(CommonFilterRules.similarLikeCountGreaterThan(1)));
        similarLikeSumLeaderboard.setRepliesInOneWeekFilter(CommonFilterRules.similarLikeSumGreaterThan(30).and(CommonFilterRules.similarLikeCountGreaterThan(0)));
        similarLikeSumLeaderboard.setRepliesInThreeDaysFilter(CommonFilterRules.similarLikeSumGreaterThan(10).and(CommonFilterRules.similarLikeCountGreaterThan(0)));

        likeLeaderBoard = new LeaderBoardEntry(Comparator.comparing(Reply::getLikeNum).reversed());
        likeLeaderBoard.setAllRepliesFilter(CommonFilterRules.likeNumGreaterThan(100));
        likeLeaderBoard.setRepliesInOneWeekFilter(CommonFilterRules.likeNumGreaterThan(80));
        likeLeaderBoard.setRepliesInThreeDaysFilter(CommonFilterRules.likeNumGreaterThan(50));

        similarCountLeaderBoard = new LeaderBoardEntry(Comparator.comparing(Reply::getSimilarCount).reversed());
        similarCountLeaderBoard.setAllRepliesFilter(CommonFilterRules.similarLikeCountGreaterThan(5));
        similarCountLeaderBoard.setRepliesInOneWeekFilter(CommonFilterRules.similarLikeCountGreaterThan(1));
        similarCountLeaderBoard.setRepliesInThreeDaysFilter(CommonFilterRules.similarLikeCountGreaterThan(0));

        refresh();
    }

    public static synchronized LeaderBoard getInstance() {
        if (instance == null) {
            synchronized (LeaderBoard.class) {
                if (instance == null) {
                    instance = new LeaderBoard();
                }
            }
        }
        return instance;
    }

    public static <T> List<T> page(List<T> list, Integer pageSize, Integer pageNum) {
        // now we only support page size as 10
        if (list == null || list.isEmpty() || pageSize != 10) {
            return null;
        }

        int recordCount = list.size();
        int pageCount = recordCount % pageSize == 0 ? recordCount / pageSize : recordCount / pageSize + 1;

        int fromIndex; //开始索引
        int toIndex; //结束索引

        if (pageNum > pageCount) {
            pageNum = pageCount;
        }

        if (!pageNum.equals(pageCount)) {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = fromIndex + pageSize;
        } else {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = recordCount;
        }

        return list.subList(fromIndex, toIndex);
    }

    public LeaderBoardEntry getSimilarLikeSumLeaderboard() {
        return similarLikeSumLeaderboard;
    }

    public LeaderBoardEntry getLikeLeaderBoard() {
        return likeLeaderBoard;
    }

    public LeaderBoardEntry getSimilarCountLeaderBoard() {
        return similarCountLeaderBoard;
    }

    public void refresh() {
        similarCountLeaderBoard.refresh();
        likeLeaderBoard.refresh();
        similarLikeSumLeaderboard.refresh();
    }

    public static class LeaderBoardEntry {
        private final Comparator<? super Reply> comparator;
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        List<Reply> allReplies;
        List<Reply> repliesInOneWeek;
        List<Reply> repliesInThreeDays;
        private Predicate<Reply> allRepliesFilter;
        private Predicate<Reply> repliesInOneWeekFilter;
        private Predicate<Reply> repliesInThreeDaysFilter;

        private LeaderBoardEntry(final Comparator<Reply> comparator) {
            this.comparator = comparator;
        }


        public void setAllRepliesFilter(final Predicate<Reply> allRepliesFilter) {
            this.allRepliesFilter = allRepliesFilter;
        }

        public void setRepliesInThreeDaysFilter(final Predicate<Reply> repliesInThreeDaysFilter) {
            this.repliesInThreeDaysFilter = repliesInThreeDaysFilter;
        }

        public void setRepliesInOneWeekFilter(final Predicate<Reply> repliesInOneWeekFilter) {
            this.repliesInOneWeekFilter = repliesInOneWeekFilter;
        }

        void refresh() {
            ComparisonDatabase.getInstance().readLock();
            rwLock.writeLock().lock();
            try {

                allReplies = ComparisonDatabase.getInstance().getReplyMap().values().stream(). //
                    filter(allRepliesFilter).sorted(comparator).collect(Collectors.toList());

                Calendar c = Calendar.getInstance();
                Date maxTime = new Date(ComparisonDatabase.getInstance().getMaxTime() * 1000L);
                c.setTime(maxTime);
                c.add(Calendar.DATE, -7);
                Predicate<Reply> timePredicate = r -> r.getCtime() * 1000L >= c.getTime().getTime();
                repliesInOneWeek = ComparisonDatabase.getInstance().getReplyMap().values().stream(). //
                    filter(repliesInOneWeekFilter.and(timePredicate)).sorted(comparator).collect(Collectors.toList());

                c.setTime(maxTime);
                c.add(Calendar.DATE, -3);
                timePredicate = r -> r.getCtime() * 1000L >= c.getTime().getTime();
                repliesInThreeDays = ComparisonDatabase.getInstance().getReplyMap().values().stream(). //
                    filter(repliesInThreeDaysFilter.and(timePredicate)).sorted(comparator).collect(Collectors.toList());
            } finally {
                ComparisonDatabase.getInstance().readUnLock();
                rwLock.writeLock().unlock();
            }
        }

        public RankingResultVo query(Predicate<Reply> filter, TimeRangeEnum timeRange, int pageSize, int pageNum) {
            ComparisonDatabase.getInstance().readLock();
            rwLock.readLock().lock();
            try {
                if (pageSize == 0 || pageNum < 1) {
                    return new RankingResultVo();
                }

                // set time filter
                List<Reply> targetReplySource = allReplies;
                switch (timeRange) {
                    case ONE_WEEK:
                        targetReplySource = repliesInOneWeek;
                        break;
                    case THREE_DAYS:
                        targetReplySource = repliesInThreeDays;
                        break;
                    default:
                        break;
                }

                List<Reply> result = targetReplySource.stream().filter(filter).collect(Collectors.toList());

                int minTime = ComparisonDatabase.getInstance().getMinTime();
                int maxTime = ComparisonDatabase.getInstance().getMaxTime();

                return new RankingResultVo(page(result, pageSize, pageNum), result.size(), minTime,
                    maxTime);
            } finally {
                ComparisonDatabase.getInstance().readUnLock();
                rwLock.readLock().unlock();
            }
        }
    }
}
