package today.wishwordrobe.weather.application;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import today.wishwordrobe.weather.dto.AirQualityDto;
import today.wishwordrobe.weather.dto.AirQualityResponse;
import today.wishwordrobe.weather.dto.IntegratedWeatherDto;
import today.wishwordrobe.weather.dto.UVIndexDto;
import today.wishwordrobe.weather.dto.UVIndexResponse;
import today.wishwordrobe.weather.dto.VillageForecastResponse;
import today.wishwordrobe.weather.dto.WeatherForecastDTO;
import today.wishwordrobe.weather.domain.Geographic;
import today.wishwordrobe.weather.util.WeatherGridConverter;
import today.wishwordrobe.weather.util.WeatherGridConverter.GridCoordinate;
import today.wishwordrobe.weather.util.LocationMapper;
import org.springframework.cache.annotation.Cacheable;
import static today.wishwordrobe.weather.configuration.CacheConfig.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WeatherClient weatherClient;
    private final ObjectMapper objectMapper;
    private final WeatherGridConverter gridConverter;
    private final WebClient airQualityClient;
    private final WebClient uvClient;
    private final LocationMapper locationMapper; 

    // 지역 좌표가 저장된 json 파일 경로 수정예정 ▶▶▶▶▶▶▶▶▶▽▶▶▶▶▶▶▶▶▶▽
    private static final String LOCATION_JSON_PATH = "static/location_data.json";

    /**
     * 지역명으로 날씨 정보 조회
     * 
     * @param location
     * @return
     */
    public Mono<WeatherForecastDTO> getWeatherForecast(String location) {
        log.info("날씨 정보 요청: {}", location);
        // 1. 지역명으로 격자 좌표 찾기
        Geographic geoLocation = findGeographicByLocationName(location);
        if (geoLocation == null) {
            log.error("지역명을 찾을수가 없습니다.: {}", location);
            return Mono.error(new RuntimeException("지역을 찾을 수 없습니다: " + location));
        }

        log.info("지역의 격자 좌표: nx={}, ny={}", geoLocation.getGridX(), geoLocation.getGridY());

        // 2.날씨 정보 조회
        return weatherClient.getVillageForecast(geoLocation)
                .map(response -> convertToWeatherForecastDTO(response, geoLocation))
                .doOnSuccess(dto -> log.info("날씨 정보 조회 성공: {}", location))
                .doOnError(e -> log.error("날씨 정보 조회 실패: {}", e.getMessage()));
    }

    /**
     * 위경도로 날씨 정보 조회
     * 
     * @param longitude 경도
     * @param latitude 위도
     * @return 날씨 정보
     */
    public Mono<WeatherForecastDTO> getWeatherByCoordinates(double longitude, double latitude) {
        log.info("위경도로 날씨 정보 요청: 경도={}, 위도={}", longitude, latitude);

        // 1. 위경도 -> 격자 좌표 변환
        GridCoordinate grid = gridConverter.toGrid(longitude, latitude);

        log.info("위경도 ({}, {}) -> 격자 ({}, {})", longitude, latitude, grid.x(), grid.y());

        // 2. 격자 좌표로 Geographic 객체 생성
        Geographic geoLocation = Geographic.builder()
                .gridX(grid.x())
                .gridY(grid.y())
                .longitude(longitude)
                .latitude(latitude)
                .country("대한민국")
                .build();

        // 3. 날씨 정보 조회
        return weatherClient.getVillageForecast(geoLocation)
                .map(response -> convertToWeatherForecastDTO(response, geoLocation))
                .doOnSuccess(dto -> log.info("위경도 기반 날씨 정보 조회 성공: ({}, {})", longitude, latitude))
                .doOnError(e -> log.error("위경도 기반 날씨 정보 조회 실패: {}", e.getMessage()));
    }
        

        
    

    private Geographic findGeographicByLocationName(String location) {
        try {
            ClassPathResource resource = new ClassPathResource(LOCATION_JSON_PATH);
            JsonNode locationData;

            try (InputStream is = resource.getInputStream()) {
                locationData = objectMapper.readTree(is);

                // 지역명으로 정보 찾기
                for (JsonNode node : locationData) {
                    // 3단계(동이름)가 일치하는지를 확인
                    if (location.equals(node.path("3단계").asText())) {
                        return Geographic.builder()
                                .id(node.has("id") ? node.path("id").asLong() : null)
                                .areaCode(node.has("행정구역코드") ? node.path("행정구역코드").asText() : null)
                                .country("대한민국")
                                .province(node.has("1단계") ? node.path("1단계").asText() : null)
                                .county(node.has("2단계") ? node.path("2단계").asText() : null)
                                .district(node.has("3단계") ? node.path("3단계").asText() : null)
                                .gridX(node.has("격자 X") ? node.path("격자 X").asLong() : 0)
                                .gridY(node.has("격자 Y") ? node.path("격자 Y").asLong() : 0)
                                .longitude(node.has("경도(초/100)") ? node.path("경도(초/100)").asDouble() : 0)
                                .latitude(node.has("위도(초/100)") ? node.path("위도(초/100)").asDouble() : 0)
                                .build();
                    }
                }
            }
        } catch (IOException e) {
            log.error("지역 정보 파일 읽기 오류: {}", e.getMessage(), e);
        }
        return null;

    }


    private WeatherForecastDTO convertToWeatherForecastDTO(VillageForecastResponse response, Geographic location) {

        // 응답에서 필요한 정보를 추출
        VillageForecastResponse.Items items = response.getResponse().getBody().getItems();

        // DTO 객체 생성해야지 맘대로 정보 가져다쓰기 쉬움
        WeatherForecastDTO.WeatherForecastDTOBuilder builder = WeatherForecastDTO.builder()
        .region(location.getCountry())
                .district(location.getDistrict())
                .province(location.getProvince())
                .county(location.getCounty())
                .areaCode(location.getAreaCode())
                .gridX(location.getGridX())
                .gridY(location.getGridY());

        // 한번만 설정하기 만들기 위해서
        boolean dateTimeSet = false;
        // API 응답이 여러 아이템을 포함하고 있는 상태 (짬뽕이다)
        for (VillageForecastResponse.Item item : items.getItem()) {
            String category = item.getCategory();
            String value = item.getFcstValue();

            // 날씨 카테고리 나눠야 나중에 temprange로 나눌수 있음
            switch (category) {
                case "TMX":
                    builder.maxTemperature(Double.parseDouble(value));
                    break;
                case "TMN": // 최저 기온
                    builder.minTemperature(Double.parseDouble(value));
                    break;
                case "REH": // 습도
                    builder.humidity(Integer.parseInt(value));
                    break;
                case "VEC": // 풍향
                    builder.windDirection(Integer.parseInt(value));
                    break;
                case "POP": // 강수확률
                    builder.precipitationProbability(Integer.parseInt(value));
                    break;
                case "SNO": // 적설량
                    builder.snowfall(value);
                    break;
                case "SKY": // 하늘상태
                    switch (value) {
                        case "1":
                            builder.skyCondition("맑음");
                            break;
                        case "3":
                            builder.skyCondition("구름많음");
                            break;
                        case "4":
                            builder.skyCondition("흐림");
                            break;
                    }
                    break;
                case "PTY": // 강수형태
                    switch (value) {
                        case "0":
                            builder.precipitationType("없음");
                            break;
                        case "1":
                            builder.precipitationType("비");
                            break;
                        case "2":
                            builder.precipitationType("비/눈");
                            break;
                        case "3":
                            builder.precipitationType("눈");
                            break;
                        case "4":
                            builder.precipitationType("소나기");
                            break;
                    }
                    break;
            }
            // 한번만 설정
            if (!dateTimeSet) {
                LocalDate baseDate = LocalDate.parse(
                        item.getBaseDate(),
                        DateTimeFormatter.ofPattern("yyyyMMdd"));
                LocalTime baseTime = LocalTime.parse(
                        item.getBaseTime(),
                        DateTimeFormatter.ofPattern("HHmm"));

                // 예보 일시 설정
                LocalDate fcstDate = LocalDate.parse(
                        item.getFcstDate(),
                        DateTimeFormatter.ofPattern("yyyyMMdd"));
                LocalTime fcstTime = LocalTime.parse(
                        item.getFcstTime(),
                        DateTimeFormatter.ofPattern("HHmm"));
                builder.baseDate(baseDate);
                builder.baseTime(baseTime);
                builder.forecastDate(fcstDate);
                builder.forecastTime(fcstTime);
                dateTimeSet = true;
            }
        }
        return builder.build();

    }

   //날씨 + 미세먼지 + 자외선 정보를 병렬로 조회 (Mono.zip 사용)
    public Mono<IntegratedWeatherDto> getIntegratedWeatherParallel(String location, String stationName, String areaNo) {
        log.info("통합 날씨 정보 병렬 조회 시작: location={}, station={}, area={}", location, stationName, areaNo);
        long startTime = System.currentTimeMillis();

        // 1. 지역 좌표 찾기
        Geographic geoLocation = findGeographicByLocationName(location);
        if (geoLocation == null) {
            log.error("지역명을 찾을 수 없습니다: {}", location);
            return Mono.error(new RuntimeException("지역을 찾을 수 없습니다: " + location));
        }

        // 2. 3개 API를 병렬로 호출 (Mono.zip)
        return Mono.zip(
            // 날씨 API
            weatherClient.getVillageForecast(geoLocation)
                .map(response -> convertToWeatherForecastDTO(response, geoLocation))
                .doOnError(e -> log.error("날씨 API 호출 실패", e))
                .onErrorResume(e -> Mono.empty()),

            // 미세먼지 API
            weatherClient.getAirQuality(stationName)
                .map(this::convertToAirQualityDto)
                .doOnError(e -> log.error("미세먼지 API 호출 실패", e))
                .onErrorResume(e -> Mono.empty()),

            // 자외선 API
            weatherClient.getUVIndex(areaNo)
                .map(this::convertToUVIndexDto)
                .doOnError(e -> log.error("자외선 API 호출 실패", e))
                .onErrorResume(e -> Mono.empty())
        )
        .map(tuple -> IntegratedWeatherDto.builder()
            .weather(tuple.getT1())      // 날씨
            .airQuality(tuple.getT2())   // 미세먼지
            .uvIndex(tuple.getT3())      // 자외선
            .location(location)
            .timestamp(java.time.LocalDateTime.now().toString())
            .build()
        )
        .doOnSuccess(dto -> {
            long totalTime = System.currentTimeMillis() - startTime;
            log.info("통합 날씨 정보 병렬 조회 완료: {} (총 {}ms)", location, totalTime);
        })
        .doOnError(error -> log.error("통합 날씨 정보 병렬 조회 실패: {}", location, error));
    }

    /**
     * 위경도 기반으로 날씨 + 미세먼지 + 자외선 정보를 병렬로 조회
     * 캐싱 적용으로 5분간 동일한 좌표 요청은 캐시에서 반환
     *
     * @param longitude 경도
     * @param latitude 위도
     * @return 통합 날씨 정보
     */
    @Cacheable(value = INTEGRATED_WEATHER_CACHE, key = "#longitude + '_' + #latitude")
    public Mono<IntegratedWeatherDto> getIntegratedWeatherByCoordinates(double longitude, double latitude) {
        log.info("위경도 기반 통합 날씨 정보 조회 시작: lon={}, lat={}", longitude, latitude);
        long startTime = System.currentTimeMillis();

        // 1. 위경도 -> 격자 좌표 변환
        GridCoordinate grid = gridConverter.toGrid(longitude, latitude);
        log.info("위경도 ({}, {}) -> 격자 ({}, {})", longitude, latitude, grid.x(), grid.y());

        // 2. 위경도 -> 측정소명/지역코드 매핑
        LocationMapper.LocationInfo locationInfo = locationMapper.getLocationInfo(longitude, latitude);
        log.info("위경도 ({}, {}) -> 측정소: {}, 지역코드: {}", longitude, latitude,
                 locationInfo.stationName(), locationInfo.areaNo());

        // 3. Geographic 객체 생성
        Geographic geoLocation = Geographic.builder()
                .gridX(grid.x())
                .gridY(grid.y())
                .longitude(longitude)
                .latitude(latitude)
                .country("대한민국")
                .build();

        // 4. 3개 API를 병렬로 호출 (Mono.zip)
        return Mono.zip(
            // 날씨 API
            weatherClient.getVillageForecast(geoLocation)
                .map(response -> convertToWeatherForecastDTO(response, geoLocation))
                .doOnError(e -> log.error("날씨 API 호출 실패: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty()),

            // 미세먼지 API
            weatherClient.getAirQuality(locationInfo.stationName())
                .map(this::convertToAirQualityDto)
                .doOnError(e -> log.error("미세먼지 API 호출 실패: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty()),

            // 자외선 API
            weatherClient.getUVIndex(locationInfo.areaNo())
                .map(this::convertToUVIndexDto)
                .doOnError(e -> log.error("자외선 API 호출 실패: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
        )
        .map(tuple -> IntegratedWeatherDto.builder()
            .weather(tuple.getT1())      // 날씨
            .airQuality(tuple.getT2())   // 미세먼지
            .uvIndex(tuple.getT3())      // 자외선
            .location(String.format("위도: %.6f, 경도: %.6f", latitude, longitude))
            .timestamp(java.time.LocalDateTime.now().toString())
            .build()
        )
        .doOnSuccess(dto -> {
            long totalTime = System.currentTimeMillis() - startTime;
            log.info("위경도 기반 통합 날씨 정보 조회 완료: ({}, {}) - 총 {}ms", longitude, latitude, totalTime);
        })
        .doOnError(error -> log.error("위경도 기반 통합 날씨 정보 조회 실패: ({}, {})", longitude, latitude, error));
    }

    /**
     * AirQualityResponse → AirQualityDto 변환
     */
    private AirQualityDto convertToAirQualityDto(AirQualityResponse response) {
        if (response == null || response.getResponse() == null || 
            response.getResponse().getBody() == null ||
            response.getResponse().getBody().getItems() == null ||
            response.getResponse().getBody().getItems().isEmpty()) {
            return null;
        }

        AirQualityResponse.Item item = response.getResponse().getBody().getItems().get(0);
        
        String khaiGradeStr = item.getKhaiGrade() != null ? item.getKhaiGrade() : "1";
        int khaiGrade = Integer.parseInt(khaiGradeStr);
        String status = getAirQualityStatus(khaiGrade);

        return AirQualityDto.builder()
            .stationName(item.getStationName())
            .dataTime(item.getDataTime())
            .khaiValue(parseIntOrZero(item.getKhaiValue()))
            .khaiGrade(khaiGrade)
            .airQualityStatus(status)
            .build();
    }

    /**
     * UVIndexResponse → UVIndexDto 변환
     */
    private UVIndexDto convertToUVIndexDto(UVIndexResponse response) {
        if (response == null || response.getResponse() == null ||
            response.getResponse().getBody() == null ||
            response.getResponse().getBody().getItems() == null ||
            response.getResponse().getBody().getItems().getItem() == null ||
            response.getResponse().getBody().getItems().getItem().isEmpty()) {
            return null;
        }

        UVIndexResponse.Item item = response.getResponse().getBody().getItems().getItem().get(0);
        
        int todayIndex = parseIntOrZero(item.getToday());
        int tomorrowIndex = parseIntOrZero(item.getTomorrow());

        return UVIndexDto.builder()
            .areaNo(item.getAreaNo())
            .date(item.getDate())
            .todayIndex(todayIndex)
            .tomorrowIndex(tomorrowIndex)
            .todayLevel(getUVLevel(todayIndex))
            .tomorrowLevel(getUVLevel(tomorrowIndex))
            .recommendation(getUVRecommendation(todayIndex))
            .build();
    }

    private int parseIntOrZero(String value) {
        try {
            return value != null ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getAirQualityStatus(int grade) {
        switch (grade) {
            case 1: return "좋음";
            case 2: return "보통";
            case 3: return "나쁨";
            case 4: return "매우나쁨";
            default: return "알 수 없음";
        }
    }

    private String getUVLevel(int index) {
        if (index <= 2) return "낮음";
        if (index <= 5) return "보통";
        if (index <= 7) return "높음";
        if (index <= 10) return "매우높음";
        return "위험";
    }

    private String getUVRecommendation(int index) {
        if (index <= 2) return "외출 시 특별한 보호조치 불필요";
        if (index <= 5) return "모자, 선글라스 착용 권장";
        if (index <= 7) return "긴팔 옷, 모자, 선글라스, 선크림 필수";
        if (index <= 10) return "가능한 실내 활동 권장, 외출 시 완전한 차단 필수";
        return "오전 10시~오후 3시 외출 자제, 긴팔+모자+선크림 필수";
    }


}
