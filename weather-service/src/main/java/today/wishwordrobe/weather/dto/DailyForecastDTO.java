package today.wishwordrobe.weather.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class DailyForecastDTO {
    private LocalDate date;
    private Double maxTemperature;
    private Double minTemperature;
    private String skyCondition;              // 15시 예보 (없으면 그날 첫 예보)
    private String precipitationType;         // 하루 중 강수가 한 번이라도 있으면 그 형태, 없으면 "없음"
    private Integer precipitationProbability; // 하루 중 최대 강수확률

}
