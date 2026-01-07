package today.wishwordrobe.weather.presentation;

import today.wishwordrobe.weather.dto.IntegratedWeatherDto;
import today.wishwordrobe.weather.dto.WeatherForecastDTO;
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
@RequestMapping("/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;

   //지역명으로 날씨 조회
    @GetMapping
    public Mono<ResponseEntity<WeatherForecastDTO>> getWeatherByLocation(@RequestParam("location") String location){
        log.info("로케이션 나와랏: {}" + location);

        return weatherService.getWeatherForecast(location)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->log.info("날씨 정보 조회 성공:{}", location))
                .onErrorResume(e -> {
                    log.error("날씨 정보 조회 오류:{}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
    //위경도로 날씨 조회
    @GetMapping("/coordinates")
    public Mono<ResponseEntity<WeatherForecastDTO>> getWeatherByCoordinates(
            @RequestParam("lat") double latitude,
            @RequestParam("lon") double longitude) {
        log.info("위경도로 날씨 조회 요청: lat={}, lon={}", latitude, longitude);

        return weatherService.getWeatherByCoordinates(longitude, latitude)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("위경도 기반 날씨 정보 조회 성공: ({}, {})", latitude, longitude))
                .onErrorResume(e -> {
                    log.error("위경도 기반 날씨 정보 조회 오류: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    //날씨 + 미세먼지 + 자외선을 병렬로 조회 (Mono.zip 사용)
    @GetMapping("/integrated")
    public Mono<ResponseEntity<IntegratedWeatherDto>> getIntegratedWeather(
            @RequestParam("location") String location,
            @RequestParam("station") String station,
            @RequestParam("areaNo") String areaNo) {
        log.info("통합 날씨 정보 병렬 조회 요청: location={}, station={}, areaNo={}", location, station, areaNo);

        return weatherService.getIntegratedWeatherParallel(location, station, areaNo)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("통합 날씨 정보 병렬 조회 성공: {}", location))
                .onErrorResume(e -> {
                    log.error("통합 날씨 정보 조회 오류: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

     //위경도로 통합 날씨 정보 조회 (날씨 + 미세먼지 + 자외선)
  
    @GetMapping("/integrated/coordinates")
    public Mono<ResponseEntity<IntegratedWeatherDto>> getIntegratedWeatherByCoordinates(
            @RequestParam("lat") double latitude,
            @RequestParam("lon") double longitude) {
        log.info("위경도로 통합 날씨 조회 요청: lat={}, lon={}", latitude, longitude);

        return weatherService.getIntegratedWeatherByCoordinates(longitude, latitude)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("위경도 기반 통합 날씨 정보 조회 성공: ({}, {})", latitude, longitude))
                .onErrorResume(e -> {
                    log.error("위경도 기반 통합 날씨 정보 조회 오류: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

}

