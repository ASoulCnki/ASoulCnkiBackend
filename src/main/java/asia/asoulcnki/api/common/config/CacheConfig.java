package asia.asoulcnki.api.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {
	@Bean("checkCache")
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		cacheManager.setCaffeine(Caffeine.newBuilder()
				// 设置最后一次写入或访问后经过固定时间过期
				.expireAfterAccess(60, TimeUnit.SECONDS)
				// 初始的缓存空间大小
				.initialCapacity(1000)
				// 缓存的最大条数
				.maximumSize(2000)
				// 减少gc压力
				.weakValues());
		return cacheManager;
	}

}