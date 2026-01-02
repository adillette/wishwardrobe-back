package today.wishwordrobe.weather.configuration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * 캐시 설정
 * API 호출 제한(429 에러) 방지를 위한 In-Memory 캐싱
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String WEATHER_CACHE = "weatherCache";
    public static final String AIR_QUALITY_CACHE = "airQualityCache";
    public static final String UV_INDEX_CACHE = "uvIndexCache";
    public static final String INTEGRATED_WEATHER_CACHE = "integratedWeatherCache";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache(WEATHER_CACHE),
            new ConcurrentMapCache(AIR_QUALITY_CACHE),
            new ConcurrentMapCache(UV_INDEX_CACHE),
            new ConcurrentMapCache(INTEGRATED_WEATHER_CACHE)
        ));
        return cacheManager;
    }
}
