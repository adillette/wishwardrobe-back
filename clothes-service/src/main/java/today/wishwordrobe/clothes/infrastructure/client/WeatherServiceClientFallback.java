package today.wishwordrobe.clothes.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import today.wishwordrobe.clothes.infrastructure.dto.WeatherResponse;

/**
 * Weather Service 호출 실패 시 대체 로직 (Circuit Breaker)
 */
@Slf4j
@Component
public class WeatherServiceClientFallback implements WeatherServiceClient {

    @Override
    public WeatherResponse getCurrentWeather(String location) {
        log.warn("Weather Service 호출 실패. Fallback 실행: location={}", location);

        // 기본 날씨 정보 반환 (평균 온도)
        WeatherResponse fallbackResponse = new WeatherResponse();
        fallbackResponse.setLocation(location);
        fallbackResponse.setTemperature(15); // 기본값: 15도 (선선한 날씨)
        fallbackResponse.setDescription("날씨 정보를 가져올 수 없습니다");
        fallbackResponse.setHumidity(50);
        fallbackResponse.setWindSpeed(0.0);

        return fallbackResponse;
    }
}
