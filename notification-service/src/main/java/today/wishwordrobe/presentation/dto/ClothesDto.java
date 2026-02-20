package today.wishwordrobe.presentation.dto;

import lombok.Data;

@Data
public class ClothesDto {
   private Long clothesId;
    private String name;
    private String category;
    private String imageUrl;
}
