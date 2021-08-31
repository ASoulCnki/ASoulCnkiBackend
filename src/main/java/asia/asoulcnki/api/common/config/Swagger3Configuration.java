package asia.asoulcnki.api.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Springfox-swagger 3.0.0配置
 *
 * @author rmym
 * @version V1.0
 */
@Configuration
public class Swagger3Configuration {
    /**
     * 是否开启swagger，生产环境一般关闭，所以这里定义一个变量
     */
    @Value("${swagger.enabled:false}")
    private Boolean enable;

    /**
     * 项目应用名
     */
    @Value("${application.name:unknown}")
    private String applicationName;

    /**
     * 项目版本信息
     */
    @Value("${application.version:1.0}")
    private String applicationVersion;

    /**
     * 项目描述信息
     */
    @Value("${application.description:none}")
    private String applicationDescription;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30).pathMapping("/")
            // 定义是否开启swagger，false为关闭，可以通过变量控制
            .enable(enable)
            // 将api的元信息设置为包含在json ResourceListing响应中。
            .apiInfo(apiInfo())
            // 选择哪些接口作为swagger的doc发布
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build();
    }

    /**
     * API 页面上半部分展示信息
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title(applicationName + " Api文档")
            .description(applicationDescription)
            .contact(new Contact("一个魂捏", null, ""))
            .version("应用版本号: " + applicationVersion + ", Spring Boot 版本号: " + SpringBootVersion.getVersion())
            .build();
    }


}
