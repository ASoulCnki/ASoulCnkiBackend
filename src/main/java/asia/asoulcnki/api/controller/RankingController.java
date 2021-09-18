package asia.asoulcnki.api.controller;

import asia.asoulcnki.api.common.duplicationcheck.FilterRulesContainer;
import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.persistence.vo.RankingResultVo;
import asia.asoulcnki.api.service.IRankingService;
import asia.asoulcnki.api.service.IRankingService.SortMethodEnum;
import asia.asoulcnki.api.service.IRankingService.TimeRangeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ranking")
@Validated
public class RankingController {

    @Autowired
    IRankingService rankingService;

    @GetMapping("/")
    @ResponseBody
    public ApiResult<RankingResultVo> getRankingResult(@RequestParam int sortMode, @RequestParam int timeRangeMode,
                                                       @RequestParam(value = "ids", required = false) List<Integer> ids, @RequestParam(value = "keywords",
        required = false) List<String> keywords, @RequestParam int pageSize, @RequestParam int pageNum) {
        // 选择排序方式和时间筛选范围
        SortMethodEnum sortMethod;
        switch (sortMode) {
            case 1:
                sortMethod = SortMethodEnum.LIKE_NUM;
                break;
            case 2:
                sortMethod = SortMethodEnum.SIMILAR_COUNT;
                break;
            default:
                sortMethod = SortMethodEnum.DEFAULT;
        }

        TimeRangeEnum timeRange;
        switch (timeRangeMode) {
            case 1:
                timeRange = TimeRangeEnum.ONE_WEEK;
                break;
            case 2:
                timeRange = TimeRangeEnum.THREE_DAYS;
                break;
            default:
                timeRange = TimeRangeEnum.ALL;
        }

        // 筛选关键词
        FilterRulesContainer container = new FilterRulesContainer();
        container.addContainsKeywordsPredicate(keywords);
        container.addUserIDInFilter(ids);

        return ApiResult.ok(rankingService.queryRankings(sortMethod, timeRange, container, pageSize, pageNum));
    }

}
