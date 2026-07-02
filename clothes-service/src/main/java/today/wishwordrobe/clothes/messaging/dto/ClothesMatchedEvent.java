package today.wishwordrobe.clothes.messaging.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import today.wishwordrobe.clothes.messaging.ClothesItemDto;

//발행용 DTO
//clothes-service가 옷 매칭 완료 후 notification-service로 보내는 dto
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor  
public class ClothesMatchedEvent {
  
  private String eventId;
  private Long userId;
  private String fcmToken;
  private Double maxTemperature;
    private Double minTemperature;
    private String skyCondition;
    private String precipitationType;
    private List<ClothesItemDto> recommendedClothes; // // 추천된 옷 목록
}
