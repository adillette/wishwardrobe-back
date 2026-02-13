package today.wishwordrobe.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WeatherServiceClientConfig {
  @Bean
  public WebClient weatherServiceWebClient(WebClient.Builder b, @Value("${weather-service.base-url}") String url) { 
    return b.baseUrl(url).build(); }
}
