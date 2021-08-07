package asia.asoulcnki.api.controller;

import asia.asoulcnki.api.common.duplicationcheck.FilterRules;
import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.vo.RankingResultVo;
import asia.asoulcnki.api.service.IRankingService;
import asia.asoulcnki.api.service.IRankingService.SortMethodEnum;
import asia.asoulcnki.api.service.IRankingService.TimeRangeEnum;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ranking")
@Validated
@CacheConfig(cacheNames = "caffeineCacheManager")
public class RankingController {

	@Autowired
	IRankingService rankingService;

	@GetMapping("/")
	@ResponseBody
	public ApiResult<RankingResultVo> getRankingResult(@RequestParam int sortMode, @RequestParam int timeRangeMode, 
            @RequestParam(value = "id", required = false) List<Integer> ids, @RequestParam int pageSize, @RequestParam int pageNum) {
       
        SortMethodEnum sortMethod = SortMethodEnum.DEFAULT;
		switch (sortMode) {
		case 1:
			sortMethod = SortMethodEnum.LIKE_NUM;
			break;
		case 2:
			sortMethod = SortMethodEnum.SIMILAR_COUNT;
			break;
		}

		TimeRangeEnum timeRange = TimeRangeEnum.ALL;
		switch (timeRangeMode) {
		case 1:
			timeRange = TimeRangeEnum.ONE_WEEK;
			break;
		case 2:
			timeRange = TimeRangeEnum.THREE_DAYS;
			break;
		}

        Predicate<Reply> filter = FilterRules.userIDIn(ids);

		return ApiResult.ok(rankingService.queryRankings(sortMethod, timeRange, filter, pageSize, pageNum));
	}

}
