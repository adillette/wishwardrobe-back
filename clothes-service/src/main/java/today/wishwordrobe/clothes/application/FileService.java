package today.wishwordrobe.clothes.application;

import today.wishwordrobe.clothes.domain.ClothesImageUploadInfo;
import today.wishwordrobe.clothes.domain.FileInfo;
import today.wishwordrobe.clothes.domain.FileUtil;
import today.wishwordrobe.exception.FileDeleteException;
import today.wishwordrobe.exception.FileUploadException;
import today.wishwordrobe.clothes.infrastructure.ClothesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;


@Service
@RequiredArgsConstructor
//@Profile("dev")
public class FileService {

    private final ClothesRepository clothesRepository;

    @Value("${file.uploadFiles}")
    private  String fileDir;

    /*
    실제 로컬에 저장하는 uploadFile 메서드
     */
    public FileInfo uploadFile(MultipartFile file, Long userId)
                throws FileUploadException {
        
        String newFileName = FileUtil.changeFileName(file);
        
        checkDirectory(userId);

        FileInfo fileInfo = createFileInfo(file,userId,newFileName);

        clothesRepository.saveFilePath(
                fileInfo.getFileName()
                ,fileInfo.getFilePath()
                ,userId);

        return fileInfo;
    }
    /*
     실제 로컬에 저장하는 uploadFiles 여러개 저장 메서드
     */
    public List<FileInfo> uploadFiles(
            List<MultipartFile> files, Long userId) throws FileUploadException {
        HashMap<String, String> newFileNames = FileUtil.changeFileNames(files);

        checkDirectory(userId);

        List<FileInfo> fileInfos = files.stream()
                .map(file-> createFileInfo(
                                file,userId,newFileNames.get(file.getOriginalFilename())))
                .collect(Collectors.toList());

        clothesRepository.saveFilePathes(fileInfos,userId);

        return fileInfos;

    }

    public void uploadImage(Long id, FileInfo fileInfo){
        ClothesImageUploadInfo imageUploadInfo =
                FileUtil.toImageUploadInfo(id,fileInfo,1);
        clothesRepository.saveImage(
                imageUploadInfo.getId(),
                imageUploadInfo.getImagePath(),
                imageUploadInfo.getImageName(),
                imageUploadInfo.getSeq()
        );
    }

    public void uploadImages(Long id, List<FileInfo> fileInfos){
        List<ClothesImageUploadInfo> imageUploadInfos = fileInfos.stream()
                .map(info -> FileUtil.toImageUploadInfo(id, info,fileInfos.indexOf(info)+1 ))
                .collect(Collectors.toList());
        clothesRepository.uploadImages(imageUploadInfos);
    }


    @Transactional(readOnly = true)
    public boolean isExistImages(Long clothesId) {

        return clothesRepository.isExistImages(clothesId);
    }


    @Transactional(readOnly = true)
    public List<ClothesImageUploadInfo> getImages(Long clothesId) {

        return clothesRepository.getImages(clothesId);
    }


    public void deleteFile(String filePath) {

        if (filePath != null) {
            new File(filePath).delete();
        }
    }


    /*
    디렉토리 삭제하기 이게 userId안에 들어있는 파일일거임 이거 해결은 어떻게 할지 고민해야함
     */
    public void deleteDirectory(Long userId) throws FileDeleteException {

        StringBuilder dirPath = new StringBuilder()
                .append(fileDir)
                .append(File.separator)
                .append(userId);

        boolean isSuccess = FileSystemUtils.deleteRecursively(new File(String.valueOf(dirPath)));

        if (!isSuccess) {
            throw new FileDeleteException("파일을 삭제하는데 실패하였습니다.");
        }
        clothesRepository.deleteFilesByUserId(userId);
    }


    public void deleteImages(Long clothesId) {
        List<String> imagePaths = clothesRepository.getImagePaths(clothesId);

        imagePaths.stream().forEach(this::deleteFile);

        clothesRepository.deleteImages(clothesId);
    }

    private void checkDirectory(Long userId) {

        StringBuilder dirPath = new StringBuilder()
                .append(fileDir)
                .append(File.separator)
                .append(userId);

        File directory = new File(String.valueOf(dirPath));

        if (!directory.exists()) {
            directory.mkdir();
        }
    }
private FileInfo createFileInfo(MultipartFile file, Long userId, String newFileName) throws FileUploadException{
        StringBuilder filePath = new StringBuilder()
                .append(fileDir)
                .append(File.separator)
                .append(userId)
                .append(File.separator)
                .append(newFileName);

        try{
            file.transferTo(new File(String.valueOf(filePath)));
            FileInfo fileInfo = new FileInfo(newFileName, String.valueOf(filePath));

            return fileInfo;
        } catch (IOException e) {
            throw new FileUploadException("파일 업로드하는데 실패하였습니다.",e);
        }
}




}
