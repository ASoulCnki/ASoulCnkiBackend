package asia.asoulcnki.api.persistence.service;

import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.persistence.vo.CheckResultVo;

public interface ICheckService {
	CheckResultVo check(String text);
}
