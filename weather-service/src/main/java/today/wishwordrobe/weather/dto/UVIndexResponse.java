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
    private List<Item> item;  // 자외선 API는 "item" 이름 사용 (items 아님)
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Item {
    private String code; // 지역코드
    private String areaNo; // 지역번호
    private String date; // 예보날짜
    private String today; // 오늘 자외선 지수 (0~11+)
    private String tomorrow; // 내일
    private String theDayAfterTomorrow; // 모레
  }
}
