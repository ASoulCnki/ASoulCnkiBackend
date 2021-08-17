package asia.asoulcnki.api.service.impl;

import asia.asoulcnki.api.common.duplicationcheck.ComparisonDatabase;
import asia.asoulcnki.api.persistence.vo.UserSpeechHistoryVO;
import asia.asoulcnki.api.service.IUserSpeechHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IUserSpeechHistoryServiceImpl implements IUserSpeechHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(IUserSpeechHistoryServiceImpl.class);

    @Override
    public UserSpeechHistoryVO getHistory(Integer mid) {
        ComparisonDatabase db = ComparisonDatabase.getInstance();
        System.err.println(db.getReply(3738762433L));
        return db.getHistory(mid);
    }
}
