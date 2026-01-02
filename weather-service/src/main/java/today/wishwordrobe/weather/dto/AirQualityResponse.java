package today.wishwordrobe.weather.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirQualityResponse {
  private Response response;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Response {
    private Header header;
    private Body body;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Header {
    private String resultCode;   // "00" 정상
    private String resultMsg;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Body {
    private List<Item> items;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Item {

    private String dataTime; // 측정일시
    private String stationName; // 측정소명
    private String khaiValue; // 통합대기환경수치
    private String khaiGrade; // 1~4
    private String khaiItem; // 주 오염물질
  }

}
