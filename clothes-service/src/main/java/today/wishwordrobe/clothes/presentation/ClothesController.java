package today.wishwordrobe.clothes.presentation;

import today.wishwordrobe.clothes.application.ClothesService;
import today.wishwordrobe.clothes.application.FileService;
import today.wishwordrobe.clothes.domain.Clothes;
import today.wishwordrobe.clothes.domain.ClothingCategory;
import today.wishwordrobe.clothes.domain.TempRange;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/clothes")
public class ClothesController{//아래 setter 바꿔야한다

    @Value("${file.uploadFiles}")
    private  String fileDir;

    private final FileService fileService;

   // private final ClothesRepository clothesRepository;
    private final ClothesService clothesService;

    public ClothesController(FileService fileService, ClothesService clothesService) {
        this.fileService = fileService;
        this.clothesService = clothesService;
    }

    /*
     ★★ 추천★★★ - MSA 방식으로 변경
     위치 정보를 받아서 Weather Service에서 날씨를 조회한 후 옷 추천
     */

    @GetMapping("/recommendations")
    public ResponseEntity<List<Clothes>> getRecommendedClothes(
            @RequestParam Long userId,
            @RequestParam String location,
            @RequestParam(required = false) ClothingCategory category  ){
        log.info("옷 추천 요청 (MSA): userId={}, location={}, category={}", userId, location, category);

        // Weather 서비스에서 날씨 정보를 가져와서 옷 추천 (Feign Client 사용)
        List<Clothes> clothes = clothesService.getClothesRecommendationByLocation(userId, location, category);

        log.info("추천된 옷 개수: {}", clothes.size());
        return ResponseEntity.ok(clothes);
    }

    /*
     ★★ 추천 (레거시) ★★★
     직접 온도를 받아서 옷 추천 - 하위 호환성 유지
     */
    @GetMapping("/recommendations/by-temperature")
    public ResponseEntity<List<Clothes>> getRecommendedClothesByTemperature(
            @RequestParam Long userId,
            @RequestParam int temperature,
            @RequestParam(required = false) ClothingCategory category  ){
        log.info("옷 추천 요청(온도 직접 입력): userId={}, temp={}, category={}", userId, temperature, category);

        TempRange tempRange = TempRange.fromTemperature(temperature);
        List<Clothes> clothes = clothesService.getClothesWithCache(userId, tempRange, category);

        return ResponseEntity.ok(clothes);
    }
    /*
    옷장에 save 시킴
     */
    @PostMapping
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
            @PathVariable Long clothesId,
            @RequestBody Clothes clothes){
        clothes.setClothesId(clothesId);
        Clothes updatedClothes = clothesService.update(clothes);

        return ResponseEntity.ok(updatedClothes);
    }

    /*
    해당 id 내용 삭제
     */
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> delete(@PathVariable Long clothesId){
        clothesService.deleteById(clothesId);
        return ResponseEntity.ok().build();
    }


    /*
      관리자용 전체 옷장조회 (캐시 없이 그냥 db 조회)
    */
    @GetMapping
    public String getAllClothes(Model model){
        List<Clothes> clothesList = clothesService.findAll();
        model.addAttribute("clothesList", clothesList);//뷰(HTML 템플릿)에서는 이 이름(clothesList)으로 데이터에 접근
        return "clothes/list";
    }



    //등록 get
    // @GetMapping("/add")
    // public String addClothes(Model model){
    //     Clothes clothes = new Clothes();
    //     model.addAttribute("clothes",clothes);
    //     return "/addClothes";
    // }

    //등록 post
    @PostMapping("/add")
    public String addClothes(@Valid
                @ModelAttribute("clothes") Clothes clothes,
                BindingResult result) {

        Clothes savedClothes = clothesService.save(clothes);
        log.info("새 옷 추가 완료: clothesId={}, userId={}", savedClothes.getClothesId(), savedClothes.getUserId());

        return "옷 등록 완료: " + savedClothes.getName();
    }

    /*
    //수정
    @GetMapping("/{clothesId}/edit")
    public String showEditForm(@PathVariable Long clothesId, Model model){
        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> new ResourceNotFoundException2("Clothes not found"));
        model.addAttribute("clothes", clothes);
        return "수정창";
        }
    @PostMapping("/{clothesId}/edit")
    public String edit(@PathVariable Long clothesId,
                       @Valid  @ModelAttribute Clothes clothes,
                       BindingResult result){
        if (result.hasErrors()) {
            return "clothes/editForm";
        }
        Clothes existClothes = clothesRepository.findById(clothesId)
                        .orElseThrow(()-> new ResourceNotFoundException2("Clothes not found"))
        clothesRepository.update(clothesId, clothes);
        return "redirect:/wishwordrobe/{clothesId}";
    }
    // 삭제 처리
    @PostMapping("/{clothesId}/delete")
    public String deleteClothes(@PathVariable Long clothesId) {
        clothesRepository.deleteById(clothesId);
        return "redirect:/wishwordrobe";
    }
    //옷 상세페이지
    @GetMapping("/{clothesId}")
    public String 메서드이름(@PathVariable Long clothesId, Model model){
        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(()-> new ResourceNotFoundException2("Clothes is not found"));
        model.addAttribute("clothes", clothes);
        return "clothes/detail";
    }

*/
}
