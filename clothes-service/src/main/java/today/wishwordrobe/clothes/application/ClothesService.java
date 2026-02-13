package today.wishwordrobe.clothes.application;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import today.wishwordrobe.clothes.domain.Clothes;
import today.wishwordrobe.clothes.domain.ClothingCategory;
import today.wishwordrobe.clothes.domain.TempRange;
import today.wishwordrobe.clothes.infrastructure.ClothesRepository;
import today.wishwordrobe.clothes.infrastructure.client.WeatherServiceClient;
import today.wishwordrobe.clothes.infrastructure.dto.WeatherResponse;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class ClothesService {

    private final ClothesRepository clothesRepository;
    private final WeatherServiceClient weatherServiceClient;

    // 위경도에 맞는추천
    public List<Clothes> getClothesRecommendationByCoordinates(
            Long userId,
            Double lat, Double lon, ClothingCategory category) {
        log.info("위경도 기반 옷 추천: userId={}, lat={}, lon={}", userId, lat, lon);
            
        WeatherResponse weather = weatherServiceClient.getWeatherByCoordinates(lat,lon);
        
        int avgTemp=0;
        if (weather.getMaxTemperature() != null && weather.getMinTemperature() != null) {
        avgTemp = (int) Math.round((weather.getMaxTemperature() + weather.getMinTemperature()) / 2.0);
    } else if (weather.getMaxTemperature() != null) {
        avgTemp = weather.getMaxTemperature().intValue();
    } else if (weather.getMinTemperature() != null) {
        avgTemp = weather.getMinTemperature().intValue();
    }
       log.info("날씨 정보 수신: 최고={}, 최저={}, 평균={}", 
             weather.getMaxTemperature(), weather.getMinTemperature(), avgTemp);
        //온도를 범위로 변환
        TempRange tempRange = TempRange.fromTemperature(avgTemp);
                
        //캐시를 사용해서 옷 조회
        List<Clothes> candidates=getClothesWithCache(userId, tempRange, category);
        return candidates.stream()
                .sorted( Comparator.comparing(Clothes::getCreatedAt))
                .limit(5)
                .collect(Collectors.toList());
    }

    // location 기반으로 날씨 정보를 가져와서 옷 추천 (MSA 동기 통신)
    public List<Clothes> getClothesRecommendationByLocation(Long userId, 
                            String location, ClothingCategory category) {
        // Weather 서비스에서 날씨 정보 가져오기 (Feign Client - 동기 통신)
        WeatherResponse weather = weatherServiceClient.getWeatherByLocation(location);

        // 온도를 범위로 변환
        TempRange tempRange = TempRange.fromTemperature(weather.getTemperature());

        List<Clothes> candidates = getClothesWithCache(userId, tempRange, category);

        // 캐시를 사용하여 옷 조회
        return candidates.stream()
                .sorted(Comparator.comparing(Clothes::getCreatedAt)) // ← 이것만 추가
                .limit(5)
                .collect(Collectors.toList());
    }

    // 특정 userId의 옷 전체 조회 (옷장 목록용)
    public List<Clothes> getClothesByUserId(Long userId) {
        return clothesRepository.findByUserId(userId);
    }

    @Cacheable(value = "clothesCache", key = "#userId+':'+ #tempRange.name() + ':' + (#category != null ? #category.name() : 'ALL')", condition = "#userId != null")
    public List<Clothes> getClothesWithCache(Long userId, TempRange tempRange, ClothingCategory category) {

        if (category != null) {
            return clothesRepository.findByUserIdAndTempRangeAndCategory(userId, tempRange, category);
        } else {
            return clothesRepository.findByUserIdAndTempRange(userId, tempRange);
        }

    }

    public Clothes save(Clothes clothes) {
        Clothes savedClothes = clothesRepository.save(clothes);
        return savedClothes;
    }

    @CacheEvict(value = "clothesCache", condition = "#userId !=null", allEntries = true)
    public void invalidateUserClothesCache(Long userId) {
        // 메서드 바디는 암것도 없는 상태가 맞음
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

}
