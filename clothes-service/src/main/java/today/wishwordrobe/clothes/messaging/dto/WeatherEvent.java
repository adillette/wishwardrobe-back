package today.wishwordrobe.clothes.messaging.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//weather-service 가 발행한 이벤트를 받는 dto
//weather -service의 WeatherEvent 와 필드 구조가 동일해야 역직렬화됨
@Getter
@Setter
@NoArgsConstructor
public class WeatherEvent {
  private Long userId;//누구 날씨인지
  private Double maxTemperature;
  private Double minTemperature;
  private String skyCondition;
  private String precipitationType;
  private String fcmToken;//누구한테 알람 보낼지

}
