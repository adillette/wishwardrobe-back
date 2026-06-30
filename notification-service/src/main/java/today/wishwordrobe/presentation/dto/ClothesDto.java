package today.wishwordrobe.presentation.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClothesDto {
   private Long clothesId;
    private String name;
    private String category;
    private String imageUrl;
}
