package today.wishwordrobe.weather.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConfigurationProperties(prefix = "weather.api")
@Getter
@Setter
public class WeatherConfig {
    private String apiKey;
    private String baseUrl;
    private String villageFcstUrl;
    private String ultraSrtFcstUrl; // 초단기예보 URL 경로
    private String ultraSrtNcstUrl; // 초단기실황 URL 경로

    @Bean
    public WebClient webClient(){
        final int size=16* 1024*1024;
        final ExchangeStrategies strategies =
                ExchangeStrategies.builder()
                        .codecs(codecs->codecs.defaultCodecs().maxInMemorySize(size))
                        .build();
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .build();
    }

}
