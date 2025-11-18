package today.wishwordrobe.clothes.infrastructure;

import today.wishwordrobe.clothes.domain.Clothes;
import today.wishwordrobe.clothes.domain.ClothesImageUploadInfo;
import today.wishwordrobe.clothes.domain.ClothingCategory;
import today.wishwordrobe.clothes.domain.FileInfo;
import today.wishwordrobe.clothes.domain.TempRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Collectors;


@Repository
public interface ClothesRepository extends JpaRepository<Clothes,Long> {

    /**
     * 회원이랑 연결하면 쓸것들
     */

    /**
     * List<Clothes> findByUserId(Long userId);
     *     List<Clothes> findByUserIdAndCategory(Long userId, ClothingCategory category);
     *     List<Clothes> findByUserIdAndTempRange(Long userId, TempRange tempRange);
     *     List<Clothes> findByUserIdAndCategoryAndTempRange(Long userId, ClothingCategory category, TempRange tempRange);
     */

    /**
     * clothes 관련
     */

    List<Clothes> findByUserIdAndTempRangeAndCategory(Long userId, TempRange tempRange, ClothingCategory category);
    List<Clothes> findByUserIdAndTempRange(Long userId, TempRange tempRange);

    @Modifying
    @Query(value=" INSERT INTO FILE_INFO(NEW_FILE_NAME, FILE_PATH, USER_ID) "+
    " VALUES(:fileName, :filePath, :userId",nativeQuery = true)

    void saveFilePath(@Param("fileName") String fileName,
                      @Param("filePath") String filePath,
                      @Param("userId") Long userId);

    default void saveFilePathes(List<FileInfo> fileInfos, Long userId){
        fileInfos.forEach(fileInfo ->
                saveFilePath(fileInfo.getFileName(),fileInfo.getFilePath(),userId));
    };


    @Modifying
    @Query(value = "INSERT INTO CLOTHES_IMAGE (ID,IMAGE_PATH,IMAGE_NAME,SEQ) "+
    " VALUES (:id, :imagePath, :imageName, :seq)", nativeQuery = true)
    void saveImage(@Param("id")Long id,
                   @Param("imagePath") String imagePath,
                   @Param("imageName")String imageName,
                   @Param("seq")int seq);

    default void uploadImages(List<ClothesImageUploadInfo> imageInfos){
        imageInfos.forEach(info->
                saveImage(info.getId(),
                        info.getImagePath(),
                        info.getImageName(),
                        info.getSeq()));

    }
    /*
    이미지 있는지 확인
     */
    @Query(value = "SELECT COUNT(*) FROM CLOTHES_IMAGE WHERE CLOTHES_ID= :clothesId", nativeQuery = true)
    int countImages(@Param("clothesId") Long clothesId);

    default boolean isExistImages(Long clothesId){
        return countImages(clothesId)>0;
    };

    /*
    이미지 정보 조회
    @Override
    @Transactional(readOnly = true)
    public List<Image> getImages(int postId) {

        return fileMapper.getImages(postId);
    }

     */
    @Query(value = "SELECT FILE_NAME, FILE_PATH, SEQ FROM CLOTHES_IMAGE "+
    " WHERE CLOTHES_ID= :clothesId ORDER BY SEQ", nativeQuery = true)
    List<Object[]> getImageData(@Param("clothesId")Long clothesId);
/*
보류
 */
    default List<ClothesImageUploadInfo> getImages(Long clothesId){
        return getImageData(clothesId).stream()
                .map(row ->new ClothesImageUploadInfo(
                        clothesId,
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).intValue()
                ))
                .collect(Collectors.toList());
    };

    // 이미지 파일 경로들만 조회
    @Query(value = "SELECT FILE_PATH FROM CLOTHES_IMAGE WHERE CLOTHES_ID = :clothesId", nativeQuery = true)
    List<String> getImagePaths(@Param("clothesId") long clothesId);



    /*
    실제 이미지 삭제
     */
    @Modifying
    @Query(value = "DELETE FROM CLOTHES_IMAGE WHERE CLOTHES_ID=:clothesId",nativeQuery = true)
    void deleteImages(@Param("clothesId") long clothesId);

    /*
    디비에 있는 이름 삭제하기
     */
    @Modifying
    @Query(value = "DELETE FROM FILE_INFO WHERE USER_ID=:userId", nativeQuery = true)
    void deleteFilesByUserId(@Param("userId") Long userId);




}
