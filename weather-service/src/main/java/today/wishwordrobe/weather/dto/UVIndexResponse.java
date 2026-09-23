package today.wishwordrobe.weather.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UVIndexResponse {
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
    private String resultCode;
    private String resultMsg;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Body {
    private Items items;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Items {
    private List<Item> item;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Item {
    private String code;     // 지수코드
    private String areaNo;   // 지점코드
    private String date;     // 발표시간
    private String h0;       // 0시간 후(현재) 예측값
    private String h3;
    private String h6;
    private String h9;
    private String h12;
    private String h15;
    private String h18;
    private String h21;
    private String h24;      // 24시간 후(내일 같은 시각) 예측값
    private String h27;
    private String h30;
    private String h33;
    private String h36;
    private String h39;
    private String h42;
    private String h45;
    private String h48;
    private String h51;
    private String h54;
    private String h57;
    private String h60;
    private String h63;
    private String h66;
    private String h69;
    private String h72;
    private String h75;
  }
}
