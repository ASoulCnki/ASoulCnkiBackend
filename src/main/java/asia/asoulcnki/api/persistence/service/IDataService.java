package asia.asoulcnki.api.persistence.service;

import asia.asoulcnki.api.persistence.vo.ControlResultVo;

public interface IDataService {
	ControlResultVo pull(int startTime);

	ControlResultVo checkpoint();

	ControlResultVo reset();
}
