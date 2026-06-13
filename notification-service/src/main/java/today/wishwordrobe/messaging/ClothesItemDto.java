package today.wishwordrobe.messaging;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClothesItemDto {

    //recommendaedClothes: clothes-service가 추천한 옷 목록
    //notification 메시지 본문에 포함해서 사용자에게 전달
    private Long clothesId;
    private String name;
    private String category;
    private String imageUrl;
}
