package asia.asoulcnki.api.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	//实例化对象
	@Bean
	public HandlerInterceptor getInterceptor() {
		return new DefaultInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(getInterceptor())
				.excludePathPatterns("/swagger**/**")
				.excludePathPatterns("/webjars/**")
				.excludePathPatterns("/v3/**")
				.excludePathPatterns("/doc.html");

	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").
				allowedOrigins("*").
				allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS").
				allowCredentials(false).maxAge(3600);
	}
}
class DefaultInterceptor implements HandlerInterceptor {

}