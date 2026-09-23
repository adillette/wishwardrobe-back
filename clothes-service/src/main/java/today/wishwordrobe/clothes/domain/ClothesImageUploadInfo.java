package today.wishwordrobe.clothes.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClothesImageUploadInfo {
    //DB의 옷 이미지 테이블에 저장하기 위한 정보
    private  Long id;  // clothesId를 의미 (이름 잘못 지음)
    private  Long clothesId;

    private String imageName;

    private  String imagePath;

    private  int seq;

    public ClothesImageUploadInfo(Long clothesId,String imageName, String imagePath, int seq) {
        this.id = null;
        this.clothesId=clothesId;
        this.imageName = imageName;
        this.imagePath = imagePath;
        this.seq = seq;
    }
    public ClothesImageUploadInfo(String imageName, String imagePath, int seq) {
        this.id = null;
        this.clothesId=null;
        this.imageName = imageName;
        this.imagePath = imagePath;
        this.seq = seq;
    }
}
