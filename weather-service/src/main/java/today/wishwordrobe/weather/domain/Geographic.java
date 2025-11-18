package today.wishwordrobe.weather.domain;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Geographic {

    @Id
    private Long id;

    private String areaCode;

    private String country;           // 국가 (예: 대한민국)
    private String province;          // 1단계: 시/도 (예: 서울특별시)
    private String county;            // 2단계: 시/군/구 (예: 종로구)
    private String district;          // 3단계: 읍/면/동 (예: 종로1동)

    // 카카오맵 API 연동 정보
    private String addressName;       // 전체 주소명
    private String region1DepthName;  // 시/도 (카카오맵 응답용)
    private String region2DepthName;  // 시/군/구 (카카오맵 응답용)
    private String region3DepthName;  // 읍/면/동 (카카오맵 응답용)

    // 위치 정보
    private Double longitude;         // 경도 (초/100)
    private Double latitude;          // 위도 (초/100)

    //기상청 격자정보
    private long gridX;
    private long gridY;

    private long longitudeHour;
    private long longitudeMinute;
    private double longitudeSecond;






}
