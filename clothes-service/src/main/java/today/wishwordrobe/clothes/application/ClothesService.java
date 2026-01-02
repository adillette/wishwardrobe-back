package today.wishwordrobe.clothes.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import today.wishwordrobe.clothes.domain.Clothes;
import today.wishwordrobe.clothes.domain.ClothingCategory;
import today.wishwordrobe.clothes.domain.TempRange;
import today.wishwordrobe.clothes.infrastructure.ClothesRepository;
import today.wishwordrobe.clothes.infrastructure.client.WeatherServiceClient;
import today.wishwordrobe.clothes.infrastructure.dto.WeatherResponse;
import today.wishwordrobe.clothes.infrastructure.messaging.ClothesEvent;
import today.wishwordrobe.clothes.infrastructure.messaging.ClothesEventProducer;

@RequiredArgsConstructor
@Service
@Transactional
public class ClothesService {
    /**
    @Qualifier("clothesCacheRedisTemplate")
    private final RedisTemplate<ClothesCacheKey, ClothesCacheValue> clothesRedisTemplate;
     */
    private final ClothesRepository clothesRepository;
    private final WeatherServiceClient weatherServiceClient;
    private final ClothesEventProducer clothesEventProducer;




    /**
     * 위치 기반으로 날씨 정보를 가져와서 옷 추천 (MSA 동기 통신)
     * @param userId 사용자 ID
     * @param location 위치 정보
     * @param category 옷 카테고리 (선택)
     * @return 추천 옷 리스트
     */
    /**
     * 특정 userId의 옷 전체 조회 (옷장 목록용)
     */
    public List<Clothes> getClothesByUserId(Long userId) {
        return clothesRepository.findByUserId(userId);
    }

    /**
     * 위치 기반으로 날씨 정보를 가져와서 옷 추천 (MSA 동기 통신)
    
     */
    public List<Clothes> getClothesRecommendationByLocation(Long userId, String location, ClothingCategory category) {
        // Weather 서비스에서 날씨 정보 가져오기 (Feign Client - 동기 통신)
        WeatherResponse weather = weatherServiceClient.getCurrentWeather(location);

        // 온도를 범위로 변환
        TempRange tempRange = TempRange.fromTemperature(weather.getTemperature());

        // 캐시를 사용하여 옷 조회
        return getClothesWithCache(userId, tempRange, category);
    }

    /**
     * 1. look aside 방식으로 사용자 옷장을 조회
     */
    @Cacheable(
            value="clothesCache",
            key="#userId+':'+ #tempRange.name() + ':' + (#category != null ? #category.name() : 'ALL')",
            condition = "#userId != null"  //** userId가 null이 아닐 때만 캐싱
    )
    public List<Clothes> getClothesWithCache(Long userId, TempRange tempRange, ClothingCategory category){
        //step 1: 캐시에서 데이터를 조회
      //  ClothesCacheKey cacheKey = ClothesCacheKey.of(userId, tempRange, category);

      //  ClothesCacheValue cachedValue = clothesRedisTemplate.opsForValue().get(userId);


        //        if(cachedValue != null){
        //            //캐시된 데이터가 1시간 이내이면 사용할수 있음
        //            if(cachedValue.getCacheTime().isAfter(LocalDateTime.now().minusHours(1)))
        //            return convertToClothes(cachedValue.getClothesList());
        //        }
        //step2: db에서 조회 후 캐시 저장 // 캐시 미스 또는 만료시 캐시에 데이터 없으면 db조회
//        List<Clothes> clothesFromDB;
//        if(category!=null){
//            clothesFromDB= clothesRepository.findByUserIdAndTempRangeAndCategory(userId, tempRange, category);
//        }else{
//            clothesFromDB = clothesRepository.findbyUserIdAndTempRange(userId,tempRange);
//        }
//
//            //step3. 데이터를 캐시에 저장
//        ClothesCacheValue cacheValue = convertToCacheValue(clothesFromDB);
//
//        clothesRedisTemplate.opsForValue().set(cacheKey,cacheValue, Duration.ofHours(2));
//
//        return clothesFromDB; //db에서 조회한 데이터 반환


        if(category !=null){
            return clothesRepository.findByUserIdAndTempRangeAndCategory(userId, tempRange, category);
         } else  {
            return clothesRepository.findByUserIdAndTempRange(userId, tempRange);
        }

    }

/*
    // Write-Behind 패턴으로 옷장 수정시 캐시 무효화
    public void invalidateUserClothesCache(Long userId){
        //해당 사용자의 모든 옷장 캐시 삭제
        String pattern ="CLOTHES::" + userId +"::*";
        Set<ClothesCacheKey> keys =clothesRedisTemplate.keys(pattern);
        if(keys!=null && !keys.isEmpty()){
            clothesRedisTemplate.delete(keys);
        }
    }
*/
    /*
    2. 저장 write- behind 적용, 저장시 캐시 무효화
     */

  
    public Clothes save(Clothes clothes){
        Clothes savedClothes = clothesRepository.save(clothes);

        // RabbitMQ로 이벤트 발행 (비동기 통신)
        // ClothesEvent event = ClothesEvent.builder()
        //         .eventType(ClothesEvent.EventType.CREATED)
        //         .clothesId(savedClothes.getClothesId())
        //         .userId(savedClothes.getUserId())
        //         .category(savedClothes.getCategory())
        //         .eventTime(LocalDateTime.now())
        //         .description("새 옷이 추가되었습니다")
        //         .build();

        // clothesEventProducer.publishEvent(event);

        return savedClothes;
    }

    @CacheEvict(
            value="clothesCache",
            condition = "#userId !=null",
            allEntries = true //모든 캐시 삭제
    )
    public void invalidateUserClothesCache(Long userId){
        //메서드 바디는 암것도 없는 상태가 맞음
    }

    /*
    3. 옷 수정
     */
  

   // clothes dto 만들기 전임
    public Clothes update(Clothes clothes){
        Clothes updatedClothes = clothesRepository.save(clothes);

        // // RabbitMQ로 이벤트 발행 (비동기 통신)
        // ClothesEvent event = ClothesEvent.builder()
        //         .eventType(ClothesEvent.EventType.UPDATED)
        //         .clothesId(updatedClothes.getClothesId())
        //         .userId(updatedClothes.getUserId())
        //         .category(updatedClothes.getCategory())
        //         .eventTime(LocalDateTime.now())
        //         .description("옷 정보가 수정되었습니다")
        //         .build();

        // clothesEventProducer.publishEvent(event);

        return updatedClothes;
    }





    /*
    4. 옷삭제
     */
   
    public void deleteById(Long clothesId){
        // 삭제 전에 옷 정보를 조회 (이벤트에 필요한 정보 확보)
        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> new IllegalArgumentException("해당 옷을 찾을 수 없습니다: " + clothesId));

        clothesRepository.deleteById(clothesId);

        // // RabbitMQ로 이벤트 발행 (비동기 통신)
        // ClothesEvent event = ClothesEvent.builder()
        //         .eventType(ClothesEvent.EventType.DELETED)
        //         .clothesId(clothesId)
        //         .userId(clothes.getUserId())
        //         .category(clothes.getCategory())
        //         .eventTime(LocalDateTime.now())
        //         .description("옷이 삭제되었습니다")
        //         .build();

        // clothesEventProducer.publishEvent(event);
    }

    /*
    5. 캐시 없이 db에서 찾기
     */
    public List<Clothes> findAll(){
        return clothesRepository.findAll();
    }

}
