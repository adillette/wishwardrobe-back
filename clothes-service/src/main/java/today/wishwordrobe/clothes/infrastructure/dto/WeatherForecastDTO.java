package today.wishwordrobe.clothes.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


import java.time.LocalDate;
import java.time.LocalTime;


@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor      // 추가: Jackson이 객체를 만들 때 필요
@AllArgsConstructor     // 추가: @Builder가 쓰는 생성자. 이게 없으면 컴파일 에러
public class WeatherForecastDTO {
    private Long id;

    //지역정보
    private String region;
    private String province;         // 시/도
    private String county;           // 시/군/구
    private String district;         // 읍/면/동
    private String areaCode;         // 행정구역코드
    private Long gridX;
    private Long gridY;

    //예보시간/ 시간 정보
    private LocalDate forecastDate;
    private LocalTime forecastTime;

    //날씨정보
    private Double maxTemperature;
    private Double minTemperature;
    private Integer humidity;
    private Integer windDirection;
    private Integer precipitationProbability;
    private String snowfall;

    private String skyCondition;
    private String precipitationType;

    private LocalDate baseDate;
    private LocalTime baseTime;

    private Long userId;
    private String fcmToken;

    private String webPushEndpoint;  // // 브라우저 푸시 서비스 URL
    private String webPushP256dh;    // // 암호화 공개키
    private String webPushAuth; 

}
