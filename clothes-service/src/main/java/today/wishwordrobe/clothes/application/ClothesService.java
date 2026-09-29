package today.wishwordrobe.clothes.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import today.wishwordrobe.clothes.domain.Clothes;
import today.wishwordrobe.clothes.domain.ClothesImageData;
import today.wishwordrobe.clothes.domain.ClothesResponse;
import today.wishwordrobe.clothes.domain.ClothingCategory;
import today.wishwordrobe.clothes.domain.TempRange;
import today.wishwordrobe.clothes.infrastructure.ClothesRepository;
import today.wishwordrobe.clothes.infrastructure.client.WeatherServiceClient;
import today.wishwordrobe.clothes.infrastructure.dto.WeatherForecastDTO;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class ClothesService {

   
    private final ClothesRepository clothesRepository;
    private final WeatherServiceClient weatherServiceClient;
    
    private static final int PER_CATEGORY_LIMIT = 2; 
    private static final int LARGE_GAP = 10;

    @Value ("${file.imageBaseUrl}")
    public String IMAGE_BASE_URL;

  
    // 추천
     @Transactional(readOnly = true)
     public List<ClothesResponse> recommendByTemperatures(
        Long userId, Double max, Double min, ClothingCategory category){

        TempRange mainRange = TempRange.fromTemperature(calcAvgTemp(max,min));
        //메인 온도 범위 저장 calcAvgTemp 기존 dto 의존성 제거하고 최고 최저 기온으로 수정하여
        //Rabbitmq 컨슈머에서 사용할수 있도록함
        TempRange outerRange;
        if(hasLargeGap(max, min)){
         outerRange= TempRange.fromTemperature(min.intValue());
        }
        else {
            outerRange= mainRange;
        }
        //Hashset으로 두번 저장안되게 만들었음
        Set<TempRange> ranges = new HashSet<>();
        ranges.addAll(withNeighbors(mainRange));
        ranges.addAll(withNeighbors(outerRange));
        List<Clothes> candidates= clothesRepository.findByUserIdAndTempRangeIn(userId, ranges);
        //db에서 온도 range에 맞는 옷들 가져오기

        List<ClothesResponse> result = new ArrayList<>();
        for(ClothingCategory c: ClothingCategory.values()){
            if(category !=null && category !=c) continue;

            TempRange t;
            if(c==ClothingCategory.OUTER){
                t=outerRange;
            }else{
                t=mainRange;
            }

        List<Clothes> picked = pick(candidates,c,t,true);
            if(picked.isEmpty()){
                picked=pick(candidates,c,t,false);
            }
            for(Clothes clothes:picked){
                result.add(toResponse(clothes));
            }
        }
        return result;

     }


    // 위경도에 맞는추천
        @Transactional(readOnly = true)
    public List<ClothesResponse> getClothesRecommendationByCoordinates(
            Long userId,
            Double lat, 
            Double lon, 
            ClothingCategory category) {
        log.info("위경도 기반 옷 추천: userId={}, lat={}, lon={}", userId, lat, lon);
            
        WeatherForecastDTO weather = weatherServiceClient.getWeatherByCoordinates(lat,lon);
         if (weather == null) {
            throw new IllegalArgumentException("Weather data is null");
        }
       
       return recommendByTemperatures(userId, weather.getMaxTemperature(), weather.getMinTemperature(), category);
        
    }

     // 레거시: 온도 하나 → 최고=최저로 넘김 (일교차 0 → 겉옷도 같은 구간)
   @Transactional(readOnly = true)
    public List<ClothesResponse> getClothesRecommendationByTemperature(
            Long userId, int temperature, ClothingCategory category) {
        return recommendByTemperatures(userId, (double) temperature, (double) temperature, category);
    }

    // location 기반으로 날씨 정보를 가져와서 옷 추천 (MSA 동기 통신)
    public List<ClothesResponse> getClothesRecommendationByLocation(Long userId, 
                            String location, ClothingCategory category) {
        // Weather 서비스에서 날씨 정보 가져오기 (Feign Client - 동기 통신)
        WeatherForecastDTO weather = weatherServiceClient.getWeatherByLocation(location);

       if (weather == null) {
            throw new IllegalArgumentException("Weather data is null");
        }
       
       return recommendByTemperatures(userId, weather.getMaxTemperature(), weather.getMinTemperature(), category);
    }


    // 특정 userId의 옷 전체 조회 (옷장 목록용)
    public List<ClothesResponse> getClothesByUserId(Long userId) {
        return clothesRepository.findByUserId(userId)
        .stream()
        .map(this::toResponse)
        .toList();
    }

    
    public Clothes save(Clothes clothes) {
        Clothes savedClothes = clothesRepository.save(clothes);
        return savedClothes;
    }
    
    
    // 3.옷수정
    public Clothes update(Clothes clothes) {
        Clothes updatedClothes = clothesRepository.save(clothes);
        
        return updatedClothes;
    }
    
    // 4.옷삭제
    public void deleteById(Long clothesId) {
        // 삭제 전에 옷 정보를 조회 (이벤트에 필요한 정보 확보)
        Clothes clothes = clothesRepository.findById(clothesId)
        .orElseThrow(() -> new IllegalArgumentException("해당 옷을 찾을 수 없습니다: " + clothesId));
        
        clothesRepository.deleteById(clothesId);
        
    }
    
    // 5.캐시없이 데이터 찾기
    public List<Clothes> findAll() {
        return clothesRepository.findAll();
    }
    
    //매 요청마다 db 직접 조회
    private List<Clothes> findCandidates(Long userId, TempRange tempRange,ClothingCategory category ){
        return (category != null)
        ? clothesRepository.findByUserIdAndTempRangeAndCategory(userId, tempRange, category)
        : clothesRepository.findByUserIdAndTempRange(userId, tempRange);
    }
    
    private int calcAvgTemp(WeatherForecastDTO weather){
        if(weather ==null){
            throw new IllegalArgumentException("Weather data is null");
        }
        
        Double max= weather.getMaxTemperature();
        Double min= weather.getMinTemperature();
        if (max == null && min == null) {
            throw new IllegalStateException("Temperature data is missing");
        }
        
        if (max != null && min != null) {
            return (int) Math.round((max + min) / 2.0);
        }
        
        return (max != null ? max : min).intValue();
    }
    
    
    private List<ClothesResponse> recommend(Long userId, TempRange tempRange, ClothingCategory category){
        List<Clothes> candidates= findCandidates(userId, tempRange, category);
        List<ClothesResponse> result = new ArrayList<>();
        
        for(ClothingCategory c: ClothingCategory.values()){
            candidates.stream()
            .filter(clothes->clothes.getCategory() == c)
            .sorted(Comparator.comparing(Clothes::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(PER_CATEGORY_LIMIT)
                .map(this::toResponse)
                .forEach(result::add);
            }
            return result;
        }
        
        private ClothesResponse toResponse(Clothes clothes){
            return new ClothesResponse(
                clothes.getClothesId(),
                clothes.getName(),
                clothes.getCategory(),
                resolveImageUrl(clothes));
            }
            
            private String resolveImageUrl(Clothes clothes){
                List<ClothesImageData> rawImages =clothesRepository.getImageData(clothes.getClothesId());
                if(!rawImages.isEmpty()){
                    return IMAGE_BASE_URL + clothes.getUserId() + "/" + rawImages.get(0).getImageName();
                }
                String stored = clothes.getImageUrl();
                if(stored !=null && !stored.isBlank()){
                    return stored;
                }
                return IMAGE_BASE_URL + "default-clothes.png";
                
            }
            //카테고리 + 구간 조건으로 골라 최신순 N개
            private List<Clothes> pick(List<Clothes> candidates, ClothingCategory c, TempRange t, boolean exact){
                return candidates.stream()
                .filter(x -> x.getCategory() == c)
                .filter(x -> exact ? x.getTempRange() == t : isNeighbor(x.getTempRange(), t))
                .sorted(Comparator.comparing(Clothes::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(PER_CATEGORY_LIMIT)
                .toList();
            }

            private List<TempRange> withNeighbors(TempRange range){
                TempRange[] all =TempRange.values();
                List<TempRange> list = new ArrayList<>();
                list.add(range);
                //.ordinal() enum열거형에서 해당 상수가 선언된 순서를 숫자로 반환하는 메서드
                //enum의 선언 순서에 따라 결정되고 enum 상수의 선언 순서 변경되면 ordinal값도 변경
                if(range.ordinal()>0) list.add(all[range.ordinal()-1]);
                //현재 온도 범위보다 한단계 낮은 온도 범위가 있으면 리스트에 추가
                if(range.ordinal()<all.length-1) list.add(all[range.ordinal()+1]);
                //현재 온도 범위보다 한단계 높은 온도 범위 있으면 리스트에 추가
                return list;
            }

            //두 온도 범위의 순서 차이 정확히 1인지 확인
            private boolean isNeighbor(TempRange a, TempRange b){
                return a!=null && Math.abs(a.ordinal()-b.ordinal())==1;
            }

            //최고기온- 아침 최저기온 온도차 10도 이상 나는지 확인하는 메서드
            private boolean hasLargeGap(Double max, Double min){
                return max!=null && min!=null && (max-min)>=LARGE_GAP;
            }

            //WeatherForecastDTO 대신 Double 2개를 받도록 변경 - 컨슈머도 같이 사용하기 위함
            private int calcAvgTemp(Double max, Double min){
                if(max==null && min==null){
                    throw new IllegalStateException("Temperature data is missing");
                }
                if(max!=null && min!=null){
                    return (int)Math.round((max+min)/2.0);
                }

                if(max!=null){
                    return max.intValue();
                }else{
                    return min.intValue();
                    //intValue() 더블 값을-> int로 변환 소수점 이하는  반올림안하고 버린다.
                }

              
            }
            
            
            // private Clothes fillImageUrl(Clothes clothes){
                //     List<ClothesImageData> rawImages=clothesRepository.getImageData(clothes.getClothesId());
                //     if(!rawImages.isEmpty()){
                    
                //         ClothesImageData first = rawImages.get(0);
                //         String imageName=first.getImageName();
                //       //  String imagePath=first.getImagePath();
                
                //         clothes.setImageUrl(IMAGE_BASE_URL +clothes.getUserId()+ "/" + imageName);
                
                //     }else{
                    //         clothes.setImageUrl(IMAGE_BASE_URL + "default-clothes.png");
                    //     }
                    //     return clothes;
                    // }
                    
                    // @Cacheable(value = "clothesCache", key = "#userId+':'+ #tempRange.name() + ':' + (#category != null ? #category.name() : 'ALL')", condition = "#userId != null")
                    // public List<Clothes> getClothesWithCache(Long userId, TempRange tempRange, ClothingCategory category) {
                        
                    //     if (category != null) {
                        //         return clothesRepository.findByUserIdAndTempRangeAndCategory(userId, tempRange, category);
                        //     } else {
                            //         return clothesRepository.findByUserIdAndTempRange(userId, tempRange);
                            //     }
                            
                            // }
                            // @CacheEvict(value = "clothesCache", condition = "#userId !=null", allEntries = true)
                            // public void invalidateUserClothesCache(Long userId) {
                            //     // 메서드 바디는 암것도 없는 상태가 맞음
                            // }
                        }
                        