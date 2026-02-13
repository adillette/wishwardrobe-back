package today.wishwordrobe.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;





@Configuration
public class ClothesServiceClientConfig {
  @Bean
  public WebClient clothesServiceWebClient(WebClient.Builder builder, @Value("${clothes-service.base-url}") String baseUrl){
    return builder.baseUrl(baseUrl).build();
  }
}
