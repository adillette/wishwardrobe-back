package today.wishwordrobe.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

/*
프론트 위경도 (lat, lon)
  ↓
WeatherGridConverter.toGrid()   ← 위경도 → 기상청 격자(nx, ny) 변환
  ↓
Geographic 객체에 gridX, gridY 담기
  ↓
WeatherClient.getVillageForecast(geoLocation)
  ↓ nx, ny 파라미터로 기상청 API 호출
  ↓
VillageForecastResponse 파싱
  ↓
WeatherForecastDTO 반환


*/



@Data
@NoArgsConstructor
@AllArgsConstructor
public class VillageForecastResponse {

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
        private String dataType;
        private Items items;
        private int pageNo;
        private int numOfRows;
        private int totalCount;
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
        private String baseDate;
        private String baseTime;
        private String category;
        private String fcstDate;
        private String fcstTime;
        private String fcstValue;
        private int nx;
        private int ny;
    }
}
