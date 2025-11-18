package today.wishwordrobe.weather.presentaion;

import today.wishwordrobe.presentation.dto.WeatherForecastDTO;
import today.wishwordrobe.weather.application.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;


    /**
     * 조회
     * @param location
     * @return
     */
    @GetMapping
    public Mono<ResponseEntity<WeatherForecastDTO>> getWeatherByLocation(@RequestParam String location){
        log.info("로케이션 나와랏: {}" + location);


            //2. weatherclient를 사용하여 날씨 정보 조회
            return weatherService.getWeatherForecast(location)
                    .map(ResponseEntity::ok)
                    .doOnSuccess(response ->log.info("날씨 정보 조회 성공:{}", location))
                    .onErrorResume(e -> {
                        log.error("날씨 정보 조회 오류:{}", e.getMessage());
                        return Mono.just(ResponseEntity.badRequest().build());
                    }); // 에러시 대체 값

    }

}



