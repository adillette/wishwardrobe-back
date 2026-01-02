package today.wishwordrobe.weather.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "airkorea.api")
@Data
public class AirKoreaConfig {
  private String apiKey;
  private String baseUrl;
  private String airQualityUrl;


  public WebClient airKoreaWebClient(){
    final int size=16*1024*1024;
    final ExchangeStrategies strategies=
    ExchangeStrategies.builder()
    .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                        .build();
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .build();
  }
}
