package today.wishwordrobe.domain;

import today.wishwordrobe.clothes.domain.ClothesInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import java.util.*;
import java.time.LocalDateTime;

@Getter
@ToString
@EqualsAndHashCode
public class ClothesCacheValue {

    private final List<ClothesInfo> clothesList;
    private final LocalDateTime cacheTime;

    public ClothesCacheValue(List<ClothesInfo> clothesList, LocalDateTime cacheTime) {
        this.clothesList = clothesList;
        this.cacheTime = cacheTime;
    }
}
