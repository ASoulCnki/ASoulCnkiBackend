package asia.asoulcnki.api.controller;

import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.persistence.param.CheckReplyParam;
import asia.asoulcnki.api.persistence.service.ICheckService;
import asia.asoulcnki.api.persistence.vo.CheckResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/check")
@Validated
@Api(tags = "检查评论")
public class CheckController {
	@Autowired
	ICheckService checkService;

	@PostMapping
	@ResponseBody
	@ApiOperation("检查评论是否重复")
	public ApiResult<CheckResultVo> checkReplyDuplication(@RequestBody @Valid CheckReplyParam param) {
		return ApiResult.ok(checkService.check(param.getText()));
	}
}
