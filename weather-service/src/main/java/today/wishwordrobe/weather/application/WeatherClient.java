package today.wishwordrobe.weather.application;

import today.wishwordrobe.presentation.dto.VillageForecastResponse;
import today.wishwordrobe.weather.configuration.WeatherConfig;
import today.wishwordrobe.weather.domain.Geographic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.*;
import java.net.URI;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherClient {
    private final WebClient webClient;
    private final WeatherConfig config;

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
                .bodyToMono(VillageForecastResponse.class);
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

}
