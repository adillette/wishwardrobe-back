package today.wishwordrobe.messaging;

import java.util.List;

import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClothesMatchedEvent {
  
  //eventId: 이 이벤트의 고유 식별자
  //outbox saveWithLock()의 중복 체크 기준
  //같은 eventId가 재전송되어도 
 // Redis Striped Lock + MongoDB Unique Index가 차단
    

  private String eventId;
  private String userId;
  private String fcmToken;
  private Double maxTemperature;
  private Double minTemperature;
  private String skyCondition;
  private String precipitationType;
  private List<ClothesItemDto> recommendedClothes; // // 추천된 옷 목록
    
    

}
