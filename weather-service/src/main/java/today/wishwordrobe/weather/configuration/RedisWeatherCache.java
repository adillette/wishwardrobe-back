package today.wishwordrobe.weather.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RedisWeatherCache {
  
  @Autowired
  private ReactiveStringRedisTemplate redis;

  @Autowired
  private ObjectMapper objectMapper= new ObjectMapper();

  public<T> Mono<T> getOrLoad(String key, Duration ttl, Class<T> type, Mono<T> loader){
     return redis.opsForValue()
        .get(key)
        .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, type)))
        .doOnError(e -> log.warn("캐시 역직렬화 실패 - key: {}, error: {}", key, e.getMessage()))
        .onErrorResume(e -> Mono.empty())
        .switchIfEmpty(
            loader.flatMap(value -> 
                Mono.fromCallable(() -> objectMapper.writeValueAsString(value))
                    .flatMap(json -> redis.opsForValue().set(key, json, ttl).thenReturn(value))
                    .doOnError(e -> log.warn("캐시 저장 실패 - key: {}, error: {}", key, e.getMessage()))
                    .onErrorReturn(value)
            )
        );
}

}
