package today.wishwordrobe.clothes.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter 
@AllArgsConstructor 
public class ClothesResponse {
    private final Long clothesId;
    private final String name;
    private final ClothingCategory category;   // "TOP" 같은 문자열로 직렬화
    private final String imageUrl;
}
