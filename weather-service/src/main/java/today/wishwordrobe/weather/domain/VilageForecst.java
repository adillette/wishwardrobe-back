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
    private long id;
    private long baseDate;
    private long fcstDate;
    private long fcstTime;
    private long gridX;
    private long gridY;
    private String category;
    private String fcstValue;

    public VilageForecst(long id, long baseDate, long fcstDate, long fcstTime, long gridX, long gridY, String category, String fcstValue) {
        this.id = id;
        this.baseDate = baseDate;
        this.fcstDate = fcstDate;
        this.fcstTime = fcstTime;
        this.gridX = gridX;
        this.gridY = gridY;
        this.category = category;
        this.fcstValue = fcstValue;
    }
}
