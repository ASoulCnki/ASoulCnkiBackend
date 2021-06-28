package asia.asoulcnki.api;

import asia.asoulcnki.api.common.duplicationcheck.ComparisonDatabase;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("asia.asoulcnki.api.persistence.mapper")
public class ApiApplication {

    public static void main(String[] args) {
        ComparisonDatabase.getInstance();
        SpringApplication.run(ApiApplication.class, args);
    }

}
