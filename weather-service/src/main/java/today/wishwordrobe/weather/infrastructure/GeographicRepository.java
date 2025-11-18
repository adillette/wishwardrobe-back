package today.wishwordrobe.weather.infrastructure;

import today.wishwordrobe.weather.domain.Geographic;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//extends ReactiveMongoRepository<Geographic,Long>
public class GeographicRepository  {

    private final Map<String, Geographic> locationCache = new ConcurrentHashMap<>();

    /*
    지역명으로 캐시에서 조회
     */
    public Mono<Geographic> findBtLocationName(String locationName){
        Geographic cached = locationCache.get(locationName);
        if(cached!=null){
            return Mono.just(cached);
        }
        return Mono.empty();

    }

    /*
    캐시에 저장
     */
    public void cache(String locationName, Geographic geographic){

        locationCache.put(locationName,geographic);
    }

    /*
   캐시 클리어
    */
    public void clearCache(){
        locationCache.clear();
    }


}



