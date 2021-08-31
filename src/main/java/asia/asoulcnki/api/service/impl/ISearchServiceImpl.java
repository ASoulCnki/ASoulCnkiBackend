package asia.asoulcnki.api.service.impl;

import asia.asoulcnki.api.common.util.EsUtil;
import asia.asoulcnki.api.persistence.param.SearchParam;
import asia.asoulcnki.api.persistence.vo.SearchResultVO;
import asia.asoulcnki.api.service.ISearchService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ISearchServiceImpl implements ISearchService {
    private static final Logger log = LoggerFactory.getLogger(ISearchServiceImpl.class);

    private static final String DEFAULT_INDEX = "as";

    @Autowired
    private EsUtil esUtil;

    @Override
    public List<SearchResultVO> search(SearchParam param) {
        QueryBuilder builder = QueryBuilders.matchQuery("content", param.getQ());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(builder);
        List<SearchResultVO> results = new ArrayList<>();
        try {
            List<Map<String, Object>> data = esUtil.searchListData(DEFAULT_INDEX, sourceBuilder, param.getSize(), param.getFrom(), "", "content", "content");
            for (Map<String, Object> datum : data) {
                results.add(JSONObject.parseObject(JSON.toJSONString(datum), SearchResultVO.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }
}
