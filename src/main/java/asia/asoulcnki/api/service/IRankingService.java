package asia.asoulcnki.api.service;

import asia.asoulcnki.api.common.duplicationcheck.FilterRulesContainer;
import asia.asoulcnki.api.persistence.vo.RankingResultVo;

public interface IRankingService {
    /**
     * 按指定规则筛选排序展示小作文
     *
     * @param sortMethod 排序方法
     * @param timeRange  筛选时间
     * @param container  过滤容器
     * @param pageSize   单页显示小作文数
     * @param pageNum    页数
     * @return
     */
    RankingResultVo queryRankings(SortMethodEnum sortMethod, final TimeRangeEnum timeRange,
                                  final FilterRulesContainer container, final int pageSize, final int pageNum);

    /**
     * 刷新排行
     */
    void refresh();

    enum SortMethodEnum {
        // 默认排序
        DEFAULT,
        // 按照点赞数排序
        LIKE_NUM,
        // 按照引用次数排序
        SIMILAR_COUNT
    }

    enum TimeRangeEnum {
        // 在所有时间内筛选
        ALL,
        // 筛选一周内
        ONE_WEEK,
        // 筛选三天内
        THREE_DAYS
    }
}
