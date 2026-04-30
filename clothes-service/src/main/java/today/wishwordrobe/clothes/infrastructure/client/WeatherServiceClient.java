package today.wishwordrobe.clothes.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import today.wishwordrobe.clothes.infrastructure.dto.WeatherForecastDTO;

/**
 * Weather Service와 통신하는 Feign Client
 * MSA 환경에서 동기 방식으로 날씨 정보를 가져옴
 */
@FeignClient(name = "weather-service", url = "${services.weather.url}", fallback = WeatherServiceClientFallback.class)
public interface WeatherServiceClient {

    // 위경도 기반 날씨 조회
    @GetMapping("/weather/coordinates")
    WeatherForecastDTO getWeatherByCoordinates(@RequestParam("lat") double lat,
            @RequestParam("lon") double lon);

    @GetMapping("/weather")
    WeatherForecastDTO getWeatherByLocation(@RequestParam("location") String locaString);

}
