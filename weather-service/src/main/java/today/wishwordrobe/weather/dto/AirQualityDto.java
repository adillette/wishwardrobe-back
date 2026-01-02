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
public class AirQualityDto {

    private String stationName;     // 측정소명
    private String dataTime;        // 측정일시
    private int pm10Value;          // 미세먼지 농도
    private int pm25Value;          // 초미세먼지 농도
    private int pm10Grade;          // 미세먼지 등급 (1:좋음, 2:보통, 3:나쁨, 4:매우나쁨)
    private int pm25Grade;          // 초미세먼지 등급
    private int khaiValue;          // 통합대기환경지수
    private int khaiGrade;          // 통합대기환경지수 등급
    private String airQualityStatus; // 종합 상태 (좋음/보통/나쁨/매우나쁨)
}
