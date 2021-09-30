package asia.asoulcnki.api.common.duplicationcheck;

import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.vo.RankingResultVo;
import asia.asoulcnki.api.service.IRankingService.TimeRangeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LeaderBoard {

    private static final Logger log = LoggerFactory.getLogger(LeaderBoard.class);

    private static volatile LeaderBoard instance;
    /**
     * 按累积赞数降序排序
     */
    private final LeaderBoardEntry similarLikeSumLeaderboard;
    /**
     * 按单评论赞数降序排序
     */
    private final LeaderBoardEntry likeLeaderBoard;
    /**
     * 按引用次数降序排序
     */
    private final LeaderBoardEntry similarCountLeaderBoard;

    /**
     * 单页最大元素数
     */
    private static final int MAX_PAGE_SIZE = 20;

    private LeaderBoard() {
        /*
         * 累积赞数要求
         * 1. 在所有的评论中，只筛选累积赞数大于50，且引用次数大于1的
         * 2. 在近7天的评论中，只筛选累积赞数大于30，且引用次数大于0的
         * 3. 在近3天的评论中，只筛选累积赞数大于10，且引用次数大于0的
         */
        similarLikeSumLeaderboard = new LeaderBoardEntry(Comparator.comparing(Reply::getSimilarLikeSum).reversed());
        similarLikeSumLeaderboard.setAllRepliesFilter(CommonFilterRules.similarLikeSumGreaterThan(50).and(CommonFilterRules.similarLikeCountGreaterThan(1)));
        similarLikeSumLeaderboard.setRepliesInOneWeekFilter(CommonFilterRules.similarLikeSumGreaterThan(30).and(CommonFilterRules.similarLikeCountGreaterThan(0)));
        similarLikeSumLeaderboard.setRepliesInThreeDaysFilter(CommonFilterRules.similarLikeSumGreaterThan(10).and(CommonFilterRules.similarLikeCountGreaterThan(0)));

        /*
         * 单点赞数要求
         * 1. 在所有的评论中，只筛选单点赞数大于100的
         * 2. 在近7天的评论中，只筛选单点赞数大于80的
         * 3. 在近3天的评论中，只筛选单点赞数大于50的
         */
        likeLeaderBoard = new LeaderBoardEntry(Comparator.comparing(Reply::getLikeNum).reversed());
        likeLeaderBoard.setAllRepliesFilter(CommonFilterRules.likeNumGreaterThan(100));
        likeLeaderBoard.setRepliesInOneWeekFilter(CommonFilterRules.likeNumGreaterThan(80));
        likeLeaderBoard.setRepliesInThreeDaysFilter(CommonFilterRules.likeNumGreaterThan(50));

        /*
         * 引用次数要求
         * 1. 在所有的评论中，只筛选引用次数大于5的
         * 2. 在近7天的评论中，只筛选引用次数大于1的
         * 3. 在近3天的评论中，只筛选应用次数大于0的
         */
        similarCountLeaderBoard = new LeaderBoardEntry(Comparator.comparing(Reply::getSimilarCount).reversed());
        similarCountLeaderBoard.setAllRepliesFilter(CommonFilterRules.similarLikeCountGreaterThan(5));
        similarCountLeaderBoard.setRepliesInOneWeekFilter(CommonFilterRules.similarLikeCountGreaterThan(1));
        similarCountLeaderBoard.setRepliesInThreeDaysFilter(CommonFilterRules.similarLikeCountGreaterThan(0));

        // 刷新以获取最新
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

    /**
     * 分页
     *
     * @param list     列表
     * @param pageSize 单页大小
     * @param pageNum  页数
     * @param <T>      列表泛型类型
     * @return 指定单页大小和页数的小作文列表
     */
    public static <T> List<T> page(List<T> list, Integer pageSize, Integer pageNum) {
        if (list == null || list.isEmpty() || pageSize <= 0) {
            return Collections.emptyList();
        }

        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        int recordCount = list.size();
        int pageCount = recordCount % pageSize == 0 ? recordCount / pageSize : recordCount / pageSize + 1;
        //开始索引
        int fromIndex;
        //结束索引
        int toIndex;

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
            log.info("start refreshing");
            try {
                // 刷新全部
                allReplies = ComparisonDatabase.getInstance().getReplyMap().values().stream(). //
                    filter(allRepliesFilter).sorted(comparator).collect(Collectors.toList());

                // 刷新7天内
                Calendar c = Calendar.getInstance();
                Date maxTime = new Date(ComparisonDatabase.getInstance().getMaxTime() * 1000L);
                c.setTime(maxTime);
                c.add(Calendar.DATE, -7);
                Predicate<Reply> timePredicate = r -> r.getCtime() * 1000L >= c.getTime().getTime();
                repliesInOneWeek = ComparisonDatabase.getInstance().getReplyMap().values().stream(). //
                    filter(repliesInOneWeekFilter.and(timePredicate)).sorted(comparator).collect(Collectors.toList());

                // 刷新3天内
                c.setTime(maxTime);
                c.add(Calendar.DATE, -3);
                timePredicate = r -> r.getCtime() * 1000L >= c.getTime().getTime();
                repliesInThreeDays = ComparisonDatabase.getInstance().getReplyMap().values().stream(). //
                    filter(repliesInThreeDaysFilter.and(timePredicate)).sorted(comparator).collect(Collectors.toList());
            } finally {
                ComparisonDatabase.getInstance().readUnLock();
                log.info("refreshing complete!");
                rwLock.writeLock().unlock();
            }
        }

        // 返回符合过滤条件的特定页小作文VO
        public RankingResultVo query(Predicate<Reply> filter, TimeRangeEnum timeRange, int pageSize, int pageNum) {
            ComparisonDatabase.getInstance().readLock();
            rwLock.readLock().lock();
            log.debug("querying articles on page {} ", pageNum);
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
