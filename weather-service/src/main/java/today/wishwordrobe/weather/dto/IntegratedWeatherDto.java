package today.wishwordrobe.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegratedWeatherDto {
    
    private WeatherForecastDTO weather;      // 날씨 정보
    private AirQualityDto airQuality;        // 미세먼지 정보
    private UVIndexDto uvIndex;              // 자외선 지수 정보
    private String location;                 // 조회 지역
    private String timestamp;                // 조회 시각
}
