package asia.asoulcnki.api.common.duplicationcheck;

import asia.asoulcnki.api.persistence.entity.Reply;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LeaderBoard {

	private final static Logger log = LoggerFactory.getLogger(LeaderBoard.class);
	private static LeaderBoard instance;
	// 累积赞数排序，至少为50，且引用次数至少为1
	private final LeaderBoardEntry likeSummaryLeaderBoard;
	// 单评论赞数排序，至少为50
	private final LeaderBoardEntry likeLeaderBoard;
	// 引用次数排序，至少为5
	private final LeaderBoardEntry similarCountLeaderBoard;

	private LeaderBoard() {
		likeSummaryLeaderBoard = new LeaderBoardEntry(r -> r.getSimilarLikeSum() > 50 && r.getSimilarCount() > 0,
				Comparator.comparing(Reply::getSimilarLikeSum).reversed());

		likeLeaderBoard = new LeaderBoardEntry(r -> r.getLikeNum() > 50,
				Comparator.comparing(Reply::getLikeNum).reversed());

		similarCountLeaderBoard = new LeaderBoardEntry(r -> r.getSimilarCount() > 5,
				Comparator.comparing(Reply::getSimilarCount).reversed());

		refresh();
	}

	public static synchronized LeaderBoard getInstance() {
		if (instance == null) {
			synchronized (LeaderBoard.class) {
				instance = new LeaderBoard();
			}
		}
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


	public LeaderBoardEntry getLikeSummaryLeaderBoard() {
		return likeSummaryLeaderBoard;
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
		likeSummaryLeaderBoard.refresh();
	}

	public static class LeaderBoardEntry {
		private final Predicate<Reply> filterPredicate;
		private final Comparator<? super Reply> comparator;
		private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

		List<Reply> sortedReplies;

		private LeaderBoardEntry(final Predicate<Reply> filterPredicate, final Comparator<Reply> comparator) {
			this.filterPredicate = filterPredicate;
			this.comparator = comparator;
		}

		public static <T> List<T> page(List<T> list, Integer pageSize, Integer pageNum) {
			// now we only support page size as 10
			if (list == null || list.isEmpty() || pageSize != 10) {
				return null;
			}

			int recordCount = list.size();
			int pageCount;

			if (recordCount % pageSize == 0) {
				pageCount = recordCount / pageSize;
			} else {
				pageCount = recordCount / pageSize + 1;
			}


			int fromIndex; //开始索引
			int toIndex = 0; //结束索引

			if (pageNum > pageCount) {
				pageNum = pageCount;
			}

			if (!pageNum.equals(pageCount)) {
				fromIndex = (pageNum - 1) * pageSize;
				toIndex = fromIndex + pageSize;
			} else {
				fromIndex = (pageNum - 1) * pageSize;
			}

			return list.subList(fromIndex, toIndex);
		}

		void refresh() {
			ComparisonDatabase.getInstance().readLock();
			rwLock.writeLock().lock();
			try {
				sortedReplies = ComparisonDatabase.getInstance().getReplyMap().values().stream(). //
						filter(filterPredicate).sorted(comparator).collect(Collectors.toList());
			} finally {
				ComparisonDatabase.getInstance().readUnLock();
				rwLock.writeLock().unlock();
			}
		}

		public List<Reply> query(Predicate<Reply> predicate, int pageSize, int pageNum) {
			rwLock.readLock().lock();
			try {
				if (pageSize != 10 || pageNum < 1) {
					return Lists.newArrayList();
				}
				List<Reply> result = sortedReplies.stream().filter(predicate).collect(Collectors.toList());
				return page(result, pageSize, pageNum);
			} finally {
				rwLock.readLock().unlock();
			}
		}
	}


}
