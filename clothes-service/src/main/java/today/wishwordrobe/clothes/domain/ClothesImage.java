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

    private final Long id;

    private final  Long clothesId;

    private final String imageName;

    private final String imagePath;

    private final int seq;


}
