package asia.asoulcnki.api.service.impl;

import asia.asoulcnki.api.common.duplicationcheck.LeaderBoard;
import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.param.QueryRandomLoveLetterParam;
import asia.asoulcnki.api.service.ILoveLetterService;
import org.springframework.stereotype.Service;
import asia.asoulcnki.api.common.duplicationcheck.LeaderBoard.LeaderBoardEntry;


import java.util.List;

@Service
public class ILoveLetterServiceImpl implements ILoveLetterService {
    @Override
    public List<Reply> queryLoveLetter(QueryRandomLoveLetterParam param){
        LeaderBoardEntry leaderBoard = LeaderBoard.getInstance().getSimilarCountLeaderBoard();
        return leaderBoard.queryLoveLetters(param);
    }
}
