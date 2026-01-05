package today.wishwordrobe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class})
@EnableCaching
@EnableFeignClients  // Feign Client 활성화 (MSA 서비스 간 통신)
public class ClothesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClothesServiceApplication.class, args);
    }
}
