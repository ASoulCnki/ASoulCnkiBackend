package asia.asoulcnki.api.controller;

import asia.asoulcnki.api.common.config.ConditionalPush;
import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.persistence.param.SearchParam;
import asia.asoulcnki.api.persistence.vo.SearchResultVO;
import asia.asoulcnki.api.service.ISearchService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@Api(tags = "搜索相关")
@Validated
public class SearchController {

    @Autowired
    ISearchService searchService;

    @GetMapping("/search")
    public ApiResult<List<SearchResultVO>> handlerSearch(@Valid SearchParam param) {

        return ApiResult.ok(searchService.search(param));
    }
}
