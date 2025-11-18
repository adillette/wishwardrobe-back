package today.wishwordrobe.clothes.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import today.wishwordrobe.clothes.infrastructure.dto.WeatherResponse;

/**
 * Weather Service와 통신하는 Feign Client
 * MSA 환경에서 동기 방식으로 날씨 정보를 가져옴
 */
@FeignClient(
        name = "weather-service",
        url = "${services.weather.url}",
        fallback = WeatherServiceClientFallback.class
)
public interface WeatherServiceClient {

    /**
     * 위치 기반으로 현재 날씨 정보 조회
     * @param location 위치 정보 (예: "Seoul", "Busan")
     * @return 날씨 정보
     */
    @GetMapping("/api/weather/current")
    WeatherResponse getCurrentWeather(@RequestParam("location") String location);
}
