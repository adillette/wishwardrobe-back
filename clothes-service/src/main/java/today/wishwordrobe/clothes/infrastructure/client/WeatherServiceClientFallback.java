package today.wishwordrobe.clothes.infrastructure.client;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import today.wishwordrobe.clothes.infrastructure.dto.WeatherForecastDTO;

//Weather Service 호출 실패 시 대체 로직 (Circuit Breaker)

@Slf4j
@Component
public class WeatherServiceClientFallback implements WeatherServiceClient {

    @Override
    public WeatherForecastDTO  getWeatherByCoordinates(double lat,double lon){
        log.warn("Weather Service 호출 실패 (좌표). Fallback 실행: lat={}, lon={}", lat, lon);
        return WeatherForecastDTO.builder()
                .maxTemperature(20.0)
                .minTemperature(10.0)
                .skyCondition("정보 없음")
                .precipitationType("없음")
                .build();
    }


    @Override
    public WeatherForecastDTO  getWeatherByLocation(String location) {
       log.warn("Weather Service 호출 실패 (지역명). Fallback 실행: location={}", location);
      return WeatherForecastDTO.builder()
                .maxTemperature(20.0)
                .minTemperature(10.0)
                .skyCondition("정보 없음")
                .precipitationType("없음")
                .build();
    }
    
}
