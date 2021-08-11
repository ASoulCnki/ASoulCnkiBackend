package asia.asoulcnki.api.service;

import asia.asoulcnki.api.persistence.param.SearchParam;
import asia.asoulcnki.api.persistence.vo.SearchResultVO;

import java.util.List;

public interface ISearchService {
    List<SearchResultVO> search(SearchParam param);
}
