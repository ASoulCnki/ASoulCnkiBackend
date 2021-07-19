package asia.asoulcnki.api.service;

import asia.asoulcnki.api.persistence.vo.ControlResultVo;

public interface IDataService {
	ControlResultVo pull(int startTime);

	ControlResultVo checkpoint();

	ControlResultVo reset();

	ControlResultVo train();
}
