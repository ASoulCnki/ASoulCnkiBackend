package asia.asoulcnki.api.service;

import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.persistence.vo.UserSpeechHistoryVO;

public interface IUserSpeechHistoryService {
    UserSpeechHistoryVO getHistory(Integer mid);
}
