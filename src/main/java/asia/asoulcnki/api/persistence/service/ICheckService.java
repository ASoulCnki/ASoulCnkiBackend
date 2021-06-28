package asia.asoulcnki.api.persistence.service;

import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.persistence.vo.CheckResultVo;

public interface ICheckService {
	public ApiResult<CheckResultVo> check(String text);
}
