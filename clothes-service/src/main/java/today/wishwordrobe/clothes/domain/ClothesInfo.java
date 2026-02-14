package today.wishwordrobe.clothes.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ClothesInfo {
    private  Long id;
    private  Long clothesId;
    private  String name;
    private  ClothingCategory category;
    private  String imageUrl;

    public ClothesInfo(Long id,Long clothesId, String name, ClothingCategory category, String imageUrl) {
        this.id = id;
        this.clothesId=clothesId;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
    }
}
