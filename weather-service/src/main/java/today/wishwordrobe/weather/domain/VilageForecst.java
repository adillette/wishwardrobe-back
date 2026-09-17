package today.wishwordrobe.weather.domain;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;



@Document
@Builder
@Getter
@NoArgsConstructor
@ToString
public class VilageForecst {

    //@GeneratedValue()
    @Id
    private String id;
    private long baseDate;
    //예보 날짜/ 시간 정보
    private long forecastDate;
    private long forecastTime;

    //지역 정보
    private long gridX;
    private long gridY;

    private String category;
    //날씨 정보
    private String fcstValue;

    public VilageForecst(String id, long baseDate, long forecastDate, long forecastTime, long gridX, long gridY, String category, String fcstValue) {
        this.id = id;
        this.baseDate = baseDate;
        this.forecastDate = forecastDate;
        this.forecastTime = forecastTime;
        this.gridX = gridX;
        this.gridY = gridY;
        this.category = category;
        this.fcstValue = fcstValue;
    }
}
