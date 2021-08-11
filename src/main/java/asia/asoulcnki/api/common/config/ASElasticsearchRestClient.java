package asia.asoulcnki.api.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author rinko
 */
@Slf4j
@Configuration
public class ASElasticsearchRestClient {
    private static final String HTTP_SCHEME = "http";
    @Value("${elasticsearch.ip}")
    String ip;
    @Value("${elasticsearch.port}")
    Integer port;

    @Bean
    public RestClientBuilder restClientBuilder() {
        return RestClient.builder(new HttpHost(ip, port, HTTP_SCHEME));
    }


    @Bean(name = "highLevelClient")
    public RestHighLevelClient highLevelClient(@Autowired RestClientBuilder restClientBuilder) {
        return new RestHighLevelClient(restClientBuilder);
    }

}

