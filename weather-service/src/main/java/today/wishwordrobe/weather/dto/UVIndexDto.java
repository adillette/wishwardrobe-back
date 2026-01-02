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
public class UVIndexDto {

    private String areaNo;          // 지역번호
    private String date;            // 예보날짜 (yyyyMMdd)
    private int todayIndex;         // 오늘 자외선 지수
    private int tomorrowIndex;      // 내일 자외선 지수
    private String todayLevel;      // 오늘 자외선 등급 (낮음/보통/높음/매우높음/위험)
    private String tomorrowLevel;   // 내일 자외선 등급
    private String recommendation;  // 추천 사항 (긴팔/모자/선크림 등)
}
