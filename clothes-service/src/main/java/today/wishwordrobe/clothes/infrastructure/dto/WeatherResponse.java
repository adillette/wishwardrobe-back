package today.wishwordrobe.clothes.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Weather 서비스로부터 받아오는 날씨 정보 DTO
 * WeatherForecastDTO 구조에 맞춰 매핑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    // 기존 필드 (호환성 유지)
    private String location;
    private int temperature;
    private String description;
    private int humidity;
    private double windSpeed;
    
    // WeatherForecastDTO 매핑용 추가 필드
    private String region;
    private String province;
    private String county;
    private String district;
    private String areaCode;
    private Long gridX;
    private Long gridY;
    
    private LocalDate forecastDate;
    private LocalTime forecastTime;
    
    private Double maxTemperature;
    private Double minTemperature;
    private Integer windDirection;
    private Integer precipitationProbability;
    private String snowfall;
    private String skyCondition;
    private String precipitationType;
    
    private LocalDate baseDate;
    private LocalTime baseTime;
}
