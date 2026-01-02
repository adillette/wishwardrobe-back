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
        private String resultCode;
        private String resultMsg;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
   
    public static class Body {
        private List<Item> items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
        public static class Item {
        // 기본 정보
        private String dataTime;          // 측정일시
        private String stationName;       // 측정소명
        private String mangName;          // 측정망 정보
        
        // 대기오염물질 농도
        private String so2Value;          // 아황산가스 농도
        private String coValue;           // 일산화탄소 농도
        private String o3Value;           // 오존 농도
        private String no2Value;          // 이산화질소 농도
        private String pm10Value;         // 미세먼지 농도
        private String pm25Value;         // 초미세먼지 농도
        
        // 등급 정보
        private String so2Grade;
        private String coGrade;
        private String o3Grade;
        private String no2Grade;
        private String pm10Grade;         // 미세먼지 등급
        private String pm25Grade;         // 초미세먼지 등급
        
        // 통합 지수
        private String khaiValue;         // 통합대기환경수치
        private String khaiGrade;         // 통합대기환경지수 등급
        
        // 플래그 (측정 중단 여부)
        private String so2Flag;
        private String coFlag;
        private String o3Flag;
        private String no2Flag;
        private String pm10Flag;
        private String pm25Flag;
    }
}
