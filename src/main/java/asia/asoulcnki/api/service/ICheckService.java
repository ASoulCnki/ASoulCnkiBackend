package asia.asoulcnki.api.service;

import asia.asoulcnki.api.persistence.vo.CheckResultVo;

public interface ICheckService {
	CheckResultVo check(String text);
}
