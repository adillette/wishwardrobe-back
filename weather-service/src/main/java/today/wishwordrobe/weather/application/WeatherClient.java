package today.wishwordrobe.weather.application;

import today.wishwordrobe.weather.dto.AirQualityResponse;
import today.wishwordrobe.weather.dto.UVIndexResponse;
import today.wishwordrobe.weather.dto.VillageForecastResponse;
import today.wishwordrobe.weather.configuration.AirKoreaConfig;
import today.wishwordrobe.weather.configuration.WeatherConfig;
import today.wishwordrobe.weather.domain.Geographic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.*;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherClient {
    private final WebClient webClient;
    private final WeatherConfig config;
    private final AirKoreaConfig airKoreaConfig;
    

    /**
     * 격자 좌표 조회
     */
    public Mono<VillageForecastResponse> getVillageForecast(Geographic location) {
        Map<String, String> baseDateTime = calculateBaseTime();
        String baseDate = baseDateTime.get("baseDate");
        String baseTime = baseDateTime.get("baseTime");

        boolean apiKeyAlreadyEncoded = config.getApiKey() != null && config.getApiKey().contains("%");

        URI uri = UriComponentsBuilder
                .fromHttpUrl(config.getBaseUrl())
                .path(config.getVillageFcstUrl()) // application.yml의 /getVilageFcst 사용
                .queryParam("serviceKey", config.getApiKey())
                .queryParam("numOfRows", 1000)
                .queryParam("pageNo", 1)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", location.getGridX())
                .queryParam("ny", location.getGridY())
                .build(apiKeyAlreadyEncoded) // 인코딩 키면 true(그대로), 디코딩 키면 false(한 번 인코딩)
                .toUri();

        log.info("생성된 api url: {}", uri);

        return webClient
                .get()
                .uri(uri) // ★ String 말고 URI로 넘기기
                .retrieve()
                .bodyToMono(VillageForecastResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                    .filter(throwable -> {
                        // 429 에러는 재시도
                        if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                            org.springframework.web.reactive.function.client.WebClientResponseException ex =
                                (org.springframework.web.reactive.function.client.WebClientResponseException) throwable;
                            return ex.getStatusCode().value() == 429;
                        }
                        return false;
                    })
                    .doBeforeRetry(retrySignal ->
                        log.warn("기상청 API 재시도 중... 시도 횟수: {}", retrySignal.totalRetries() + 1)
                    )
                );
    }

    private Map<String, String> calculateBaseTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        int[] baseTimes = { 2, 5, 8, 11, 14, 17, 20, 23 };
        int hour = now.getHour();
        int minute = now.getMinute();

        int baseTimeIndex = -1;
        for (int i = baseTimes.length - 1; i >= 0; i--) {
            if (hour > baseTimes[i] || (hour == baseTimes[i] && minute >= 10)) {
                baseTimeIndex = i;
                break;
            }
        }

        Map<String, String> result = new HashMap<>();

        // 발표 시각이 없는 경우 (0시~2시 10분) 전날 마지막 발표 사용
        if (baseTimeIndex == -1) {
            result.put("baseDate", now.minusDays(1).format(dateFormatter));
            result.put("baseTime", "2300");
        } else {
            result.put("baseDate", now.format(dateFormatter));
            result.put("baseTime", String.format("%02d00", baseTimes[baseTimeIndex]));
        }

        return result;

    }

    //미세먼지
    public Mono<AirQualityResponse> getAirQuality(String stationName){
        String apiKey = airKoreaConfig.getApiKey();
        String encodedApiKey = apiKey == null ? null
                : (apiKey.contains("%") ? apiKey : UriUtils.encodeQueryParam(apiKey, StandardCharsets.UTF_8));
        String encodedStationName = stationName == null ? null
                : UriUtils.encodeQueryParam(stationName, StandardCharsets.UTF_8);

        URI uri = UriComponentsBuilder
        .fromUriString(airKoreaConfig.getBaseUrl() + airKoreaConfig.getAirQualityUrl())
        .queryParam("serviceKey", encodedApiKey)
        .queryParam("returnType", "json")
        .queryParam("stationName", encodedStationName)
        .queryParam("dataTerm", "DAILY")
        .queryParam("ver", "1.0")  // ← 이거 추가!
        .queryParam("pageNo", 1)
        .queryParam("numOfRows", 1)
        .build(true).toUri();

        log.info("생성된 미세먼지 api url: {}", uri);

        return webClient.get().uri(uri).retrieve()
        .bodyToMono(AirQualityResponse.class)
        .doOnSuccess(response -> {
            // ===== 여기부터 추가 =====
            log.info("===== 미세먼지 API 응답 =====");
            if (response != null && response.getResponse() != null) {
                log.info("resultCode: {}", response.getResponse().getHeader().getResultCode());
                log.info("resultMsg: {}", response.getResponse().getHeader().getResultMsg());
                
                if (response.getResponse().getBody() != null) {
                    int totalCount = response.getResponse().getBody().getTotalCount();
                    int itemsSize = response.getResponse().getBody().getItems() != null ? 
                                    response.getResponse().getBody().getItems().size() : 0;
                    
                    log.info("totalCount: {}", totalCount);
                    log.info("items size: {}", itemsSize);
                    
                    if (response.getResponse().getBody().getItems() != null && 
                        !response.getResponse().getBody().getItems().isEmpty()) {
                        log.info("첫 번째 item: {}", response.getResponse().getBody().getItems().get(0));
                    }
                }
            }
            log.info("=============================");
            // ===== 여기까지 추가 =====
        })
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
            .filter(throwable -> {
                if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                    org.springframework.web.reactive.function.client.WebClientResponseException ex =
                        (org.springframework.web.reactive.function.client.WebClientResponseException) throwable;
                    return ex.getStatusCode().value() == 429;
                }
                return false;
            })
            .doBeforeRetry(retrySignal ->
                log.warn("미세먼지 API 재시도 중... 시도 횟수: {}", retrySignal.totalRetries() + 1)
            )
        );
    }

    //자외선 지수
    public Mono<UVIndexResponse> getUVIndex(String areaNo){
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        URI uri = UriComponentsBuilder
        .fromUriString("http://apis.data.go.kr/1360000/LivingWthrIdxServiceV4/getUVIdxV4")
        .queryParam("serviceKey", config.getApiKey())
        .queryParam("areaNo",areaNo)
        .queryParam("time", today)
        .queryParam("dataType", "JSON")
        .build(true).toUri();

        return webClient.get().uri(uri).retrieve()
        .bodyToMono(UVIndexResponse.class)
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
            .filter(throwable -> {
                if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                    org.springframework.web.reactive.function.client.WebClientResponseException ex =
                        (org.springframework.web.reactive.function.client.WebClientResponseException) throwable;
                    return ex.getStatusCode().value() == 429;
                }
                return false;
            })
            .doBeforeRetry(retrySignal ->
                log.warn("자외선 API 재시도 중... 시도 횟수: {}", retrySignal.totalRetries() + 1)
            )
        );
    }

}
