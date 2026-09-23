package today.wishwordrobe.clothes.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileInfo {// 로컬에 저장된 파일의 결과 정보를 담는 단순 dto

    private  String fileName;
    private  String filePath;
}
