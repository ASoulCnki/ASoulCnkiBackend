package asia.asoulcnki.api.service;

import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.param.QueryRandomLoveLetterParam;

import java.util.List;

public interface ILoveLetterService {
    List<Reply> queryLoveLetter(QueryRandomLoveLetterParam param);
}
