package asia.asoulcnki.api.controller.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Configuration
@EnableScheduling
public class StaticTaskScheduler {
	private final static Logger log = LoggerFactory.getLogger(StaticTaskScheduler.class);

	@Scheduled(fixedRate = 5000)
	private void configureTasks() {
//		log.info("scheduler is triggered");
	}
}
