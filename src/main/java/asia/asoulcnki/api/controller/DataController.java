package asia.asoulcnki.api.controller;

import asia.asoulcnki.api.common.BizException;
import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.common.response.CnkiCommonEnum;
import asia.asoulcnki.api.persistence.param.ControlParam;
import asia.asoulcnki.api.persistence.service.IDataService;
import asia.asoulcnki.api.persistence.vo.ControlResultVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/data")
@Validated
public class DataController {

	@Autowired
	IDataService dataService;
	@Value("${secure.key}")
	private String secureKey;

	void checkSecureKey(String requestSecureKey) {
		if (StringUtils.isBlank(requestSecureKey) || !requestSecureKey.equals(secureKey)) {
			throw new BizException(CnkiCommonEnum.AUTH);
		}
	}

	@PostMapping("/pull")
	@ResponseBody
	public ApiResult<ControlResultVo> pullDataFromDatabase(@Valid @RequestBody ControlParam param) {
		checkSecureKey(param.getSecureKey());
		return ApiResult.ok(dataService.pull(param.getStartTime()));
	}

	@PostMapping("/reset")
	@ResponseBody
	public ApiResult<ControlResultVo> reset(@RequestBody ControlParam param) {
		checkSecureKey(param.getSecureKey());
		return ApiResult.ok(dataService.reset());
	}

	@PostMapping("/checkpoint")
	@ResponseBody
	public ApiResult<ControlResultVo> checkpoint(@RequestBody ControlParam param) {
		checkSecureKey(param.getSecureKey());
		return ApiResult.ok(dataService.checkpoint());
	}

}
