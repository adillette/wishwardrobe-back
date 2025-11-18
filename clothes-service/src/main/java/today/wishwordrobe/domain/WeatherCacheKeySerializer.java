package today.wishwordrobe.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Objects;
import java.nio.charset.Charset;

@Slf4j
public class WeatherCacheKeySerializer implements RedisSerializer<WeatherCacheKey> {

    private final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    public byte[] serialize(WeatherCacheKey weatherCacheKey) throws SerializationException {

        if(Objects.isNull(weatherCacheKey))

            throw new SerializationException("ClothesCacheKey is null");


        log.info("어디가 문제인줄은 알아야지 weatherskeyserializer null이다. serializer를 찾아라");

        return weatherCacheKey.toString().getBytes(UTF_8);
    }

    @Override
    public WeatherCacheKey deserialize(byte[] bytes) throws SerializationException {
        if(Objects.isNull(bytes))
            throw new SerializationException("bytes is null");

        return WeatherCacheKey.fromString(new String(bytes,UTF_8));
    }
}
