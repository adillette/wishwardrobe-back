package today.wishwordrobe.clothes.presentation;

import today.wishwordrobe.clothes.application.ClothesService;
import today.wishwordrobe.clothes.domain.Clothes;
import today.wishwordrobe.clothes.domain.ClothingCategory;
import today.wishwordrobe.clothes.domain.TempRange;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/clothes")
public class ClothesController{//아래 setter 바꿔야한다
    private final ClothesService clothesService;

    public ClothesController(ClothesService clothesService) {
        this.clothesService = clothesService;
    }

  
    /*
     ★★ 추천★★★ - MSA 방식으로 변경
     위치 정보를 받아서 Weather Service에서 날씨를 조회한 후 옷 추천
     */

    @GetMapping("/recommendations/coordinates")
    public ResponseEntity<List<Clothes>> getRecommendedClothesByCoordinates(
            @RequestParam("userId") Long userId,
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam(value = "category", required = false) ClothingCategory category  ){
        
        log.info("옷 추천 요청 (위경도): userId={}, lat={}, lon={}, category={}", 
                 userId, lat, lon, category);

        List<Clothes> clothes = clothesService.getClothesRecommendationByCoordinates(
                userId, lat, lon, category);

        log.info("추천된 옷 개수: {}", clothes.size());
        return ResponseEntity.ok(clothes);
    }


    /*
     ★★ 추천 (레거시) ★★★
     직접 온도를 받아서 옷 추천 - 하위 호환성 유지
     */
    @GetMapping("/recommendations/by-temperature")
    public ResponseEntity<List<Clothes>> getRecommendedClothesByTemperature(
            @RequestParam("userId") Long userId,
            @RequestParam("temperature") int temperature,
            @RequestParam(value = "category", required = false) ClothingCategory category  ){
        log.info("옷 추천 요청(온도 직접 입력): userId={}, temp={}, category={}", userId, temperature, category);

        TempRange tempRange = TempRange.fromTemperature(temperature);
        List<Clothes> clothes = clothesService.getClothesWithCache(userId, tempRange, category);

        return ResponseEntity.ok(clothes);
    }
    /*
    옷장에 save 시킴
     */
    @PostMapping("/add")
    public ResponseEntity<Clothes> addClothes(@RequestBody Clothes clothes){
        //save 메서드가 자동으로 캐시 무효화 처리해줌
        Clothes savedClothes = clothesService.save(clothes);

        log.info("새옷 추가 및 캐시 무효화 완료: userId={}" ,clothes.getUserId());
        return ResponseEntity.ok(savedClothes);
    }

    /*
    userid로 업데이트
    clothes 엔티티 id값 -> 6/25 clothesId로 변경함
     */
    @PutMapping("/{clothesId}")
    public ResponseEntity<Clothes> updateClothes(
            @PathVariable("clothesId") Long clothesId,
            @RequestBody Clothes clothes){
        clothes.setClothesId(clothesId);
        Clothes updatedClothes = clothesService.update(clothes);

        return ResponseEntity.ok(updatedClothes);
    }

    /*
    해당 id 내용 삭제
     */
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> delete(@PathVariable("clothesId") Long clothesId){
        clothesService.deleteById(clothesId);
        return ResponseEntity.ok().build();
    }
}
