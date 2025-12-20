package today.wishwordrobe.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Objects;
import java.nio.charset.Charset;

@Slf4j
public class ClothesCacheKeySerializer implements RedisSerializer<ClothesCacheKey> {

    //여기서 override든 뭐든 하는이유는 utf-8로 저장시키고 역직렬화에서 개같은 에러가 안나게 하고자함이다.

    private final Charset UTF_8 = Charset.forName("UTF-8");


    @Override
    public byte[] serialize(ClothesCacheKey clothesCacheKey) throws SerializationException {
        if(Objects.isNull(clothesCacheKey))

            throw new SerializationException("ClothesCacheKey is null");


            log.info("어디가 문제인줄은 알아야지 clothesCachekey가 null이다. serializer를 찾아라");

            return clothesCacheKey.toString().getBytes(UTF_8);



    }

    @Override
    public ClothesCacheKey deserialize(byte[] bytes) throws SerializationException {

        if(Objects.isNull(bytes))

            throw new SerializationException("bytes  is null");

        return ClothesCacheKey.fromString(new String(bytes,UTF_8));
    }
}
