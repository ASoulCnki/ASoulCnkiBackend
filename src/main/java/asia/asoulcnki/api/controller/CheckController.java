package asia.asoulcnki.api.controller;

import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.persistence.param.CheckReplyParam;
import asia.asoulcnki.api.persistence.service.ICheckService;
import asia.asoulcnki.api.persistence.vo.CheckResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/check")
@Validated
public class CheckController {
	@Autowired
	ICheckService checkService;

	@PostMapping
	@ResponseBody
	public ApiResult<CheckResultVo> checkReplyDuplication(@RequestBody CheckReplyParam param) {
		return ApiResult.ok(checkService.check(param.getText()));
	}
}
