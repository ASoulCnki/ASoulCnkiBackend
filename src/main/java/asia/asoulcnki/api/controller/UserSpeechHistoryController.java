package asia.asoulcnki.api.controller;

import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.persistence.vo.UserSpeechHistoryVO;
import asia.asoulcnki.api.service.IUserSpeechHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history")
@Validated
public class UserSpeechHistoryController {

    @Autowired
    private IUserSpeechHistoryService iUserSpeechHistoryService;

    @GetMapping("/{mid}")
    public ApiResult<UserSpeechHistoryVO> handlerGetHistory(@PathVariable("mid") Integer mid){
        return ApiResult.ok(iUserSpeechHistoryService.getHistory(mid));
    }
}
