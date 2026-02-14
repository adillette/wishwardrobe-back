package today.wishwordrobe.clothes.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ClothesImage {

    private  Long id;

    private Long clothesId;

    private String imageName;

    private String imagePath;

    private int seq;


}
