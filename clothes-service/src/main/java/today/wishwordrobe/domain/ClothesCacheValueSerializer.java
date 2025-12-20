package today.wishwordrobe.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Objects;
import java.nio.charset.Charset;

@Slf4j
public class ClothesCacheValueSerializer implements RedisSerializer<ClothesCacheValue> {

    public static final ObjectMapper om = new ObjectMapper();
    private final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    public byte[] serialize(ClothesCacheValue clothesCacheValue) throws SerializationException {
      if(Objects.isNull(clothesCacheValue))
          return null;
      try {
          String json = om.writeValueAsString(clothesCacheValue);
          return json.getBytes(UTF_8);
      } catch (JsonProcessingException e) {
          throw new SerializationException("json serialize error", e);
      }

    }

    @Override
    public ClothesCacheValue deserialize(byte[] bytes) throws SerializationException {

        if(Objects.isNull(bytes))
            return null;
        try{
          return om.readValue(new String(bytes,UTF_8),ClothesCacheValue.class);
        } catch (JsonProcessingException e) {
            throw new SerializationException("json deserialize error", e);

        }

    }
}
