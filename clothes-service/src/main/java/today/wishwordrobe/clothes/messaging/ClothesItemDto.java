package today.wishwordrobe.clothes.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClothesItemDto {
   private Long clothesId;
   private String name;
   private String category;
   private String imageUrl;
}
