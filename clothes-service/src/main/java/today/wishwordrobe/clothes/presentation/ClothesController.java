package today.wishwordrobe.clothes.presentation;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import today.wishwordrobe.clothes.application.ClothesService;
import today.wishwordrobe.clothes.application.FileService;
import today.wishwordrobe.clothes.domain.Clothes;
import today.wishwordrobe.clothes.domain.ClothesResponse;
import today.wishwordrobe.clothes.domain.ClothingCategory;
import today.wishwordrobe.clothes.domain.FileInfo;
import today.wishwordrobe.clothes.domain.TempRange;
import today.wishwordrobe.user.domain.AuthUser;

@Slf4j
@RestController
@RequestMapping("/clothes")
public class ClothesController {// 아래 setter 바꿔야한다
    private final ClothesService clothesService;
    private final FileService fileService;

    public ClothesController(ClothesService clothesService, FileService fileService) {
        this.clothesService = clothesService;
        this.fileService = fileService;
    }

    /*
     * ★★ 추천★★★ - MSA 방식으로 변경
     * 위치 정보를 받아서 Weather Service에서 날씨를 조회한 후 옷 추천
     */

    @GetMapping("/recommendations/coordinates")
    public ResponseEntity<List<ClothesResponse>> getRecommendedClothesByCoordinates(
            @RequestAttribute("authUser") AuthUser authUser,
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam(value = "category", required = false) ClothingCategory category) {

        log.info("옷 추천 요청 (위경도): userId={}, lat={}, lon={}, category={}",
                authUser.getUserId(), lat, lon, category);

        List<ClothesResponse> clothes = clothesService.getClothesRecommendationByCoordinates(
                authUser.getUserId(), lat, lon, category);

        log.info("추천된 옷 개수: {}", clothes.size());
        return ResponseEntity.ok(clothes);
    }

    /*
     * ★★ 추천 (레거시) ★★★
     * 직접 온도를 받아서 옷 추천 - 하위 호환성 유지
     */
    @GetMapping("/recommendations/by-temperature")
    public ResponseEntity<List<ClothesResponse>> getRecommendedClothesByTemperature(
            @RequestAttribute("authUser") AuthUser authUser,
            @RequestParam("temperature") int temperature,
            @RequestParam(value = "category", required = false) ClothingCategory category) {
        log.info("옷 추천 요청(온도 직접 입력): userId={}, temp={}, category={}", authUser.getUserId(), temperature, category);

        List<ClothesResponse> clothes = clothesService.getClothesRecommendationByTemperature(
                authUser.getUserId(), temperature, category);

        return ResponseEntity.ok(clothes);
    }

    /*
     * 옷장에 save 시킴
     */
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Clothes> addClothes(
            @RequestAttribute("authUser") AuthUser authUser,
            @RequestParam("name") String name,
            @RequestParam("category") ClothingCategory category,
            @RequestParam("tempRange") TempRange tempRange,
            @RequestParam("image") MultipartFile image) {
        // save 메서드가 자동으로 캐시 무효화 처리해줌
        Clothes clothes = Clothes.builder()
                .userId(authUser.getUserId())
                .name(name)
                .category(category)
                .tempRange(tempRange)
                .build();

        Clothes savedClothes = clothesService.save(clothes);

        FileInfo fileInfo = fileService.uploadFile(image, authUser.getUserId());
        fileService.uploadImage(savedClothes.getClothesId(), fileInfo);
        log.info("새옷 추가 및 이미지 저장 완료: userId={}, clothesId={}", authUser.getUserId(), savedClothes.getClothesId());
        return ResponseEntity.ok(savedClothes);
    }

    /*
     * userid로 업데이트
     * clothes 엔티티 id값 -> 6/25 clothesId로 변경함
     */
    @PutMapping(value = "/{clothesId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothesResponse> updateClothes(
        @PathVariable("clothesId") Long clothesId,
        @RequestAttribute("authUser") AuthUser authUser,
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "category", required = false) ClothingCategory category,
        @RequestParam(value = "tempRange", required = false) TempRange tempRange,
        @RequestParam(value = "image", required = false) MultipartFile image) {

       ClothesResponse updated = clothesService.update(
            clothesId, authUser.getUserId(), name, category, tempRange, image);
    
            return ResponseEntity.ok(updated);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ClothesResponse>> getClothesByUserId(@PathVariable("userId") Long userId,
            @RequestAttribute("authUser") AuthUser authUser) {
        
        if (!Objects.equals(authUser.getUserId(), userId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        log.info("옷장 전체 조회: userId={}", userId);
        List<ClothesResponse> clothes = clothesService.getClothesByUserId(userId);
        return ResponseEntity.ok(clothes);
    }

    /*
     * 해당 id 내용 삭제
     */
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> delete(@PathVariable ("clothesId") Long clothesId,
            @RequestAttribute("authUser") AuthUser authUser) {
        clothesService.deleteById(clothesId, authUser.getUserId());
        return ResponseEntity.ok().build();
    }
}
