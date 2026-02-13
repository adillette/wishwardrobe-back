package today.wishwordrobe.clothes.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import today.wishwordrobe.clothes.infrastructure.dto.WeatherResponse;

//Weather Service 호출 실패 시 대체 로직 (Circuit Breaker)

@Slf4j
@Component
public class WeatherServiceClientFallback implements WeatherServiceClient {

    @Override
    public WeatherResponse getWeatherByCoordinates(double lat,double lon){
        log.warn("Weather Service 호출 실패 (좌표). Fallback 실행: lat={}, lon={}", lat, lon);
        return createDefaultWeatherResponse(String.format("위도: %.4f, 경도: %.4f", lat, lon));
    }


    @Override
    public WeatherResponse getWeatherByLocation(String location) {
       log.warn("Weather Service 호출 실패 (지역명). Fallback 실행: location={}", location);
      return createDefaultWeatherResponse(location);
    }
    private WeatherResponse createDefaultWeatherResponse(String location){
        WeatherResponse fallbackResponse = new WeatherResponse();
        fallbackResponse.setLocation(location);
        fallbackResponse.setMaxTemperature(20.0);  // 기본 최고 온도
        fallbackResponse.setMinTemperature(10.0);  // 기본 최저 온도
        fallbackResponse.setDescription("날씨 정보를 가져올 수 없습니다");
        fallbackResponse.setHumidity(50);
        fallbackResponse.setSkyCondition("정보 없음");
        fallbackResponse.setPrecipitationType("없음");
        return fallbackResponse;
    }
}
