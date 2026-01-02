package today.wishwordrobe.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class ApiUrlGenerateDTO {
    private String serviceKey;
    private int numOfRows;
    private int pageNo;
    private int baseDate;
    private int baseTime;
    private String dataType;
    private int nx;
    private int ny;

    private String areaCode;

}
