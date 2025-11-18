package today.wishwordrobe.configuration;

import today.wishwordrobe.domain.*;
import today.wishwordrobe.domain.WeatherCacheKey;
import today.wishwordrobe.domain.WeatherCacheKeySerializer;
import today.wishwordrobe.domain.WeatherCacheValue;
import today.wishwordrobe.domain.WeatherCacheValueSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(
            @org.springframework.beans.factory.annotation.Qualifier("cacheRedisConnectionFactory1") RedisConnectionFactory redisConnectionFactory) {

        // Redis 캐시 설정
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))  // 30분 TTL
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
   @Bean
   public RedisConnectionFactory cacheRedisConnectionFactory0(){
       RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("127.0.0.1", 6379);
       configuration.setDatabase(0);
        //        configuration.setUsername("username");
        //        configuration.setPassword("password");

        final SocketOptions socketOptions = SocketOptions.builder().connectTimeout(Duration.ofSeconds(10)).build();
        final ClientOptions clientOptions = ClientOptions.builder().socketOptions(socketOptions).build();

       LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
               .clientOptions(clientOptions)
               .commandTimeout(Duration.ofSeconds(10))
               .shutdownTimeout(Duration.ZERO)
               .build();

        return new LettuceConnectionFactory(configuration, lettuceClientConfiguration);

   }




    /*
    1. 고객 서비스

     */



    /*
      2.  옷장서비스
        */

    @Bean
    public RedisConnectionFactory cacheRedisConnectionFactory1(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("127.0.0.1", 6379);
        configuration.setDatabase(1);
        //        configuration.setUsername("username");
        //        configuration.setPassword("password");

        final SocketOptions socketOptions = SocketOptions.builder().connectTimeout(Duration.ofSeconds(10)).build();
        final ClientOptions clientOptions = ClientOptions.builder().socketOptions(socketOptions).build();

        LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofSeconds(10))
                .shutdownTimeout(Duration.ZERO)
                .build();

        return new LettuceConnectionFactory(configuration, lettuceClientConfiguration);

    }


    @Bean(name = "clothesCacheRedisTemplate")
    public RedisTemplate<ClothesCacheKey, ClothesCacheValue> clothesCacheRedisTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("cacheRedisConnectionFactory1") RedisConnectionFactory connectionFactory){

                RedisTemplate<ClothesCacheKey, ClothesCacheValue> template = new RedisTemplate<>();

                template.setConnectionFactory(connectionFactory);

                template.setKeySerializer(new ClothesCacheKeySerializer());

                template.setValueSerializer(new ClothesCacheValueSerializer());

                return template;

    }


     /*
     3.  날씨서비스
       */
     @Bean
     public RedisConnectionFactory cacheRedisConnectionFactory2(){
         RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("127.0.0.1", 6379);
         configuration.setDatabase(2);
         //        configuration.setUsername("username");
         //        configuration.setPassword("password");

         final SocketOptions socketOptions = SocketOptions.builder().connectTimeout(Duration.ofSeconds(10)).build();
         final ClientOptions clientOptions = ClientOptions.builder().socketOptions(socketOptions).build();

         LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
                 .clientOptions(clientOptions)
                 .commandTimeout(Duration.ofSeconds(10))
                 .shutdownTimeout(Duration.ZERO)
                 .build();

         return new LettuceConnectionFactory(configuration, lettuceClientConfiguration);

     }

    @Bean(name = "weatherCacheRedisTemplate")
    public RedisTemplate<WeatherCacheKey, WeatherCacheValue> weatherCacheRedisTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("cacheRedisConnectionFactory2") RedisConnectionFactory connectionFactory){

        RedisTemplate<WeatherCacheKey, WeatherCacheValue> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new WeatherCacheKeySerializer());

        template.setValueSerializer(new WeatherCacheValueSerializer());

        return template;

    }


     /*
     4.  알림서비스
       */

    @Bean
    public RedisConnectionFactory cacheRedisConnectionFactory3(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("127.0.0.1", 6379);
        configuration.setDatabase(3);
        //        configuration.setUsername("username");
        //        configuration.setPassword("password");

        final SocketOptions socketOptions = SocketOptions.builder().connectTimeout(Duration.ofSeconds(10)).build();
        final ClientOptions clientOptions = ClientOptions.builder().socketOptions(socketOptions).build();

        LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofSeconds(10))
                .shutdownTimeout(Duration.ZERO)
                .build();

        return new LettuceConnectionFactory(configuration, lettuceClientConfiguration);

    }

    /*
    단순 조회용
     */
    @Bean(name = "stringRedisTemplate")
    public StringRedisTemplate stringRedisTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("cacheRedisConnectionFactory0") RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        log.info("StringRedisTemplate 생성 완료");
        return template;
    }

    // 기본 redisTemplate (Spring Data Redis가 요구)
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("cacheRedisConnectionFactory1") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        log.info("RedisTemplate 생성 완료");
        return template;
    }


}
