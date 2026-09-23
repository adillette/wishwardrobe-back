package today.wishwordrobe.clothes.infrastructure;

import today.wishwordrobe.clothes.domain.Clothes;
import today.wishwordrobe.clothes.domain.ClothesImageData;
import today.wishwordrobe.clothes.domain.ClothesImageUploadInfo;
import today.wishwordrobe.clothes.domain.ClothesInfo;
import today.wishwordrobe.clothes.domain.ClothingCategory;
import today.wishwordrobe.clothes.domain.FileInfo;
import today.wishwordrobe.clothes.domain.TempRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;



@Repository 
public interface ClothesRepository extends JpaRepository<Clothes,Long> {

    /**
     * 회원이랑 연결하면 쓸것들
     */
    List<Clothes> findByUserId(Long userId);
    List<Clothes> findByUserIdAndCategory(Long userId, ClothingCategory category);
    List<Clothes> findByUserIdAndTempRangeAndCategory(Long userId, TempRange tempRange, ClothingCategory category);
    List<Clothes> findByUserIdAndTempRange(Long userId, TempRange tempRange);

    @Query("SELECT new today.wishwordrobe.clothes.domain.ClothesInfo(" +
       "c.clothesId, c.name, c.category, c.imageUrl) " +
       "FROM Clothes c WHERE c.clothesId = :clothesId")
    ClothesInfo getClothesInfo(@Param("clothesId")Long clothesId);
    



    @Modifying
    @Query(value = "INSERT INTO file_info(new_file_name, file_path, user_id) " +
            "VALUES(:fileName, :filePath, :userId)", nativeQuery = true)
    void saveFilePath(@Param("fileName") String fileName,
                    @Param("filePath") String filePath,
                    @Param("userId") Long userId);

    default void saveFilePathes(List<FileInfo> fileInfos, Long userId){
        fileInfos.forEach(fileInfo ->
                saveFilePath(fileInfo.getFileName(),fileInfo.getFilePath(),userId));
    };


    @Modifying
    @Query(value = "INSERT INTO clothes_image (clothes_id, image_path, image_name, seq) " +
            "VALUES (:clothesId, :imagePath, :imageName, :seq)", nativeQuery = true)
    void saveImage(@Param("clothesId") Long clothesId,
                @Param("imagePath") String imagePath,
                @Param("imageName") String imageName,
                @Param("seq") int seq);

    default void uploadImages(Long clothesId,List<ClothesImageUploadInfo> imageInfos){
        imageInfos.forEach(info->
                saveImage(
                        clothesId,
                        info.getImagePath(),
                        info.getImageName(),
                        info.getSeq()));

    }
    /*
    이미지 있는지 확인
     */
    @Query(value = "SELECT COUNT(*) FROM clothes_image WHERE clothes_id = :clothesId", nativeQuery = true)
    int countImages(@Param("clothesId") Long clothesId);

    default boolean isExistImages(Long clothesId){
        return countImages(clothesId)>0;
    };

   //이미지 조회
@Query(value = "SELECT image_name AS imageName, image_path AS imagePath, seq AS seq " +
    "FROM clothes_image WHERE clothes_id = :clothesId ORDER BY seq", nativeQuery = true)
List<ClothesImageData> getImageData(@Param("clothesId") Long clothesId);


// 이미지 파일 경로들만 조회
@Query(value = "SELECT image_path FROM clothes_image WHERE clothes_id = :clothesId", nativeQuery = true)
List<String> getImagePaths(@Param("clothesId") long clothesId);


    /*
    실제 이미지 삭제
     */
    @Modifying
    @Query(value = "DELETE FROM clothes_image WHERE clothes_id = :clothesId", nativeQuery = true)
    void deleteImages(@Param("clothesId") long clothesId);

    /*
    디비에 있는 이름 삭제하기
     */
    @Modifying
    @Query(value = "DELETE FROM file_info WHERE user_id = :userId", nativeQuery = true)
    void deleteFilesByUserId(@Param("userId") Long userId);
    
    




}
