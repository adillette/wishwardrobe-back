package today.wishwordrobe.clothes.domain;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.UUID;

public class FileUtil {
    public static String changeFileName(MultipartFile file){
        return String.valueOf(createNewFileName(file));
    }

    public static HashMap<String, String> changeFileNames(List<MultipartFile> files){

        HashMap<String, String> newFileNames = new HashMap<>();

        for(MultipartFile file: files){
            newFileNames.put(file.getOriginalFilename(),String.valueOf(createNewFileName(file)));
        }
        return newFileNames;
    }

    public static ClothesImageUploadInfo toImageUploadInfo(Long id,
                                                           FileInfo fileinfo, int seq){
        return new ClothesImageUploadInfo(id,fileinfo.getFileName(),
                fileinfo.getFilePath(),seq);
    }

    private static StringBuilder createNewFileName(MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        StringBuilder newFileName = new StringBuilder()
                .append(uuid)
                .append(".")
                .append(extension);

        return newFileName;
    }
}
