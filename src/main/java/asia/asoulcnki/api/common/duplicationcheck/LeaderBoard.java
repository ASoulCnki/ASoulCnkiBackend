package asia.asoulcnki.api.common.duplicationcheck;

import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.vo.RankingResultVo;
import asia.asoulcnki.api.service.IRankingService.TimeRangeEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LeaderBoard {

	private final static Logger log = LoggerFactory.getLogger(LeaderBoard.class);

    private List<Integer> userIDs;

	private static LeaderBoard instance;
	// 累积赞数排序，至少为50，且引用次数至少为1
	private final LeaderBoardEntry similarLikeSumLeaderboard;
	// 单评论赞数排序，至少为50
	private final LeaderBoardEntry likeLeaderBoard;
	// 引用次数排序，至少为5
	private final LeaderBoardEntry similarCountLeaderBoard;

	private LeaderBoard(List<Integer> userIDs) {
        this.userIDs = userIDs;

		similarLikeSumLeaderboard = new LeaderBoardEntry(Comparator.comparing(Reply::getSimilarLikeSum).reversed());
		similarLikeSumLeaderboard.setRepliesInUsersFilter(this.userIDs);
        similarLikeSumLeaderboard.setAllRepliesFilter(FilterRules.similarLikeSumGreaterThan(100).and(FilterRules.similarLikeCountGreaterThan(1)));
		similarLikeSumLeaderboard.setRepliesInOneWeekFilter(FilterRules.similarLikeSumGreaterThan(80).and(FilterRules.similarLikeCountGreaterThan(0)));
		similarLikeSumLeaderboard.setRepliesInThreeDaysFilter(FilterRules.similarLikeSumGreaterThan(50).and(FilterRules.similarLikeCountGreaterThan(0)));

		likeLeaderBoard = new LeaderBoardEntry(Comparator.comparing(Reply::getLikeNum).reversed());
		likeLeaderBoard.setRepliesInUsersFilter(this.userIDs);
        likeLeaderBoard.setAllRepliesFilter(FilterRules.likeNumGreaterThan(300));
		likeLeaderBoard.setRepliesInOneWeekFilter(FilterRules.likeNumGreaterThan(150));
		likeLeaderBoard.setRepliesInThreeDaysFilter(FilterRules.likeNumGreaterThan(100));

		similarCountLeaderBoard = new LeaderBoardEntry(Comparator.comparing(Reply::getSimilarCount).reversed());
		similarCountLeaderBoard.setRepliesInUsersFilter(this.userIDs);
        similarCountLeaderBoard.setAllRepliesFilter(FilterRules.similarLikeCountGreaterThan(5));
		similarCountLeaderBoard.setRepliesInOneWeekFilter(FilterRules.similarLikeCountGreaterThan(1));
		similarCountLeaderBoard.setRepliesInThreeDaysFilter(FilterRules.similarLikeCountGreaterThan(0));

		refresh();
	}

	public static synchronized LeaderBoard getInstance(List<Integer> userIDs) {
		if (instance == null) {
			synchronized (LeaderBoard.class) {
				instance = new LeaderBoard(userIDs);
                return instance;
			}
		}
        instance.setUserIDs(userIDs);
        instance.refresh();
		return instance;
	}

	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		long result = calendar.getTimeInMillis();
		System.out.println(result / 1000);
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

    public void setUserIDs(List<Integer> userIDs) {
        this.userIDs = userIDs;
    }

	public void refresh() {
        similarCountLeaderBoard.setRepliesInUsersFilter(this.userIDs);
		similarCountLeaderBoard.refresh();

        likeLeaderBoard.setRepliesInUsersFilter(this.userIDs);
		likeLeaderBoard.refresh();

        similarLikeSumLeaderboard.setRepliesInUsersFilter(this.userIDs);
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
        private Predicate<Reply> repliesInUsersFilter;

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

        public void setRepliesInUsersFilter(List<Integer> users) {
            if (users == null) {
                this.repliesInUsersFilter = FilterRules.alwaysTrue(0);
                return;
            }
            this.repliesInUsersFilter = FilterRules.userIDIn(users);
        }

		void refresh() {
			ComparisonDatabase.getInstance().readLock();
			rwLock.writeLock().lock();
			try {

				allReplies = ComparisonDatabase.getInstance().getReplyMap().values().stream(). //
						filter(allRepliesFilter).sorted(comparator).collect(Collectors.toList());

                allReplies.removeIf(repliesInUsersFilter);

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

		public RankingResultVo query(TimeRangeEnum timeRange, int pageSize, int pageNum) {
			ComparisonDatabase.getInstance().readLock();
			rwLock.readLock().lock();
			try {
				if (pageSize != 10 || pageNum < 1) {
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
				}

				int minTime = ComparisonDatabase.getInstance().getMinTime();
				int maxTime = ComparisonDatabase.getInstance().getMaxTime();
                
                List<Reply> replies = new ArrayList<>();
                replies.addAll(page(targetReplySource, pageSize, pageNum));
                
				return new RankingResultVo(replies, targetReplySource.size(), minTime, maxTime);
			} finally {
				ComparisonDatabase.getInstance().readUnLock();
				rwLock.readLock().unlock();
			}
		}
	}
}
