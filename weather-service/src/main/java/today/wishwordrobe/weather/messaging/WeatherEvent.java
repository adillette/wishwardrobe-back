package today.wishwordrobe.weather.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeatherEvent {

  //eventId 이 이벤트의 고유 식별자
  //UUID로 생성 -> clothes-service-> notification-service까지 전달
  //notification-service outbox의 중복 체크 기준
  private String eventId;
  private Long userId;
  private double lat;
  private double lon;
  private Double maxTemperature;
  private Double minTemperature;
  private String skyCondition;
  private String precipitationType;
  
 



}
