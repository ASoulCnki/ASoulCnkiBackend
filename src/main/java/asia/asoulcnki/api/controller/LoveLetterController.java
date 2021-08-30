package asia.asoulcnki.api.controller;

import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.param.QueryRandomLoveLetterParam;
import asia.asoulcnki.api.service.ILoveLetterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
public class LoveLetterController {
    @Autowired
    private ILoveLetterService loveLetterService;

    @GetMapping("/loveletter/random")
    @ResponseBody
    public ApiResult<List<Reply>> queryLoveLetter(QueryRandomLoveLetterParam param) {
        return ApiResult.ok(loveLetterService.queryLoveLetter(param));
    }

}
