package today.wishwordrobe.weather.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class ApiDataDTO {
    private String baseDate;
    private String baseTime;
    private String category;
    private String fcstDate;
    private String fcstValue;
    private String nx;
    private String ny;
}
