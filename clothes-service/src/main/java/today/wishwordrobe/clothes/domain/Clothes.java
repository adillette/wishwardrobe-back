package today.wishwordrobe.clothes.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import jakarta.persistence.*;

@Entity
@Table(name="clothes")
@NoArgsConstructor
@Getter
@Setter
public class Clothes {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long clothesId;

    @Column(name="user_id")
    private Long userId;

    private String name;

    @Enumerated(EnumType.STRING)
    private ClothingCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name="temp_range")
    private TempRange tempRange;

    private String imageUrl;

    //이거 필요한지 체크해서 없애야할지 고민할것
    //private MultipartFile clothesImage;

    @Builder
    public Clothes(Long userId, String name, ClothingCategory category, TempRange tempRange,String imageUrl){
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.tempRange = tempRange;
        this.imageUrl = imageUrl;

    }


}
