package today.wishwordrobe.weather.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import today.wishwordrobe.weather.dto.AirQualityDto;
import today.wishwordrobe.weather.dto.UVIndexDto;


@Document 
@Getter 
@Setter 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherTotal {

    private String id;

    //지역정보
    private String region;
    private String province;         // 시/도
    private String county;           // 시/군/구
    private String district;         // 읍/면/동
    private String areaCode;         // 행정구역코드
    private Long gridX;
    private Long gridY;

    //예보시간/ 시간 정보
    private LocalDate forecastDate;
    private LocalTime forecastTime;

    //날씨정보
    private Double maxTemperature;
    private Double minTemperature;
    private Integer humidity;
    private Integer windDirection;
    private Integer precipitationProbability;
    private String snowfall;

    private String skyCondition;
    private String precipitationType;

    private LocalDate baseDate;
    private LocalTime baseTime;

    //미세먼지
    private AirQualityDto airQuality;
   
    //자외선
    private UVIndexDto uvIndex;

    private LocalDateTime createdAt;
}
