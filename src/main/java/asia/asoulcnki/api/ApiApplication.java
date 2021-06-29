package asia.asoulcnki.api;

import asia.asoulcnki.api.common.duplicationcheck.ComparisonDatabase;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableCaching
@MapperScan("asia.asoulcnki.api.persistence.mapper")
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Component
	public static class JobAfterStart {
		@PostConstruct // 构造函数之后执行
		public void initComparisonDatabase() {
			ComparisonDatabase.getInstance();
		}
	}
}
