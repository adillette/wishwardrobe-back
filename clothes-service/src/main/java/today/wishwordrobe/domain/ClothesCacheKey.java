package today.wishwordrobe.domain;

import today.wishwordrobe.clothes.domain.ClothingCategory;
import today.wishwordrobe.clothes.domain.TempRange;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ClothesCacheKey {

    private static final String PREFIX= "CLOTHES::";

    private final Long userId;
    private final today.wishwordrobe.clothes.domain.TempRange tempRange;
    private final ClothingCategory category;




    public ClothesCacheKey(Long userId,TempRange tempRange, ClothingCategory category) {
        if(Objects.isNull(userId)) throw new IllegalArgumentException("userId can't be null");
        if(Objects.isNull(tempRange)) throw new IllegalArgumentException("tempRange can't be null");
        if(Objects.isNull(category)) throw new IllegalArgumentException("category can't be null");
        this.userId = userId;
        this.tempRange = tempRange;
        this.category = category;

    }


    public static ClothesCacheKey from(Long userId,TempRange tempRange, ClothingCategory category) {
        return new ClothesCacheKey(userId,tempRange,category);
    }

    public static ClothesCacheKey fromString(String key) {

        String[] tokens = key.split("::");
        if(tokens.length!=4){
            throw new IllegalArgumentException("Invalid key format");
        }


        Long userId = Long.valueOf(tokens[1]);
        TempRange tempRange = TempRange.valueOf(tokens[2]);
        ClothingCategory category = ClothingCategory.valueOf(tokens[3]);

        return ClothesCacheKey.from(userId,tempRange,category);

    }

    public static ClothesCacheKey of(Long userId,TempRange tempRange, ClothingCategory category) {
        return new ClothesCacheKey(userId,tempRange,category);
    }

    @Override
    public String toString() {
        return PREFIX + userId +"::" + tempRange.name() +"::" + category.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClothesCacheKey that = (ClothesCacheKey) o;
        return Objects.equals(userId, that.userId) &&
                tempRange == that.tempRange &&
                category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, tempRange, category);
    }

}
