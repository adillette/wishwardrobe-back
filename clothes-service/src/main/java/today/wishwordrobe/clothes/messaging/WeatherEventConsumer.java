package today.wishwordrobe.clothes.messaging;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import today.wishwordrobe.clothes.application.ClothesService;
import today.wishwordrobe.clothes.domain.Clothes;
import today.wishwordrobe.clothes.domain.ClothesInfo;
import today.wishwordrobe.clothes.domain.TempRange;
import today.wishwordrobe.clothes.messaging.dto.ClothesMatchedEvent;
import today.wishwordrobe.clothes.messaging.dto.WeatherEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherEventConsumer {
  private final ClothesService clothesService;
  private final ClothesMatchedEventPublisher clothesMatchedEventPublisher;

  @RabbitListener(queues =RabbitMQConfig.WEATHER_QUEUE)
  public void handleWeatherEvent(WeatherEvent event){
    //weather.clothes.queue에서 메시지 수신 시 자동 호출
    //Sprint이 Json -> weatherEvent 객체로 역직렬화
    log.info("WeatherEvent 수신. userId={}, maxTemp={}, minTemp={}",
                event.getUserId(), event.getMaxTemperature(), event.getMinTemperature());

   Long userId = Long.parseLong(event.getUserId())             ;
   //평균 기온 계산후 TempRange 변환
   int avgTemp= calAvgTemp(event);

    TempRange tempRange = TempRange.fromTemperature(avgTemp);
    List<Clothes> candidates = clothesService.getClothesWithCache(userId, tempRange, null);
    List<ClothesItemDto> recommendedClothes   =candidates.stream()
                                            .sorted(Comparator.comparing(Clothes::getCreatedAt))
                                            .limit(5)
                                            .map(this::toDto)
                                            .collect(Collectors.toList());
    
    ClothesMatchedEvent matchedEvent= new ClothesMatchedEvent(
      event.getUserId(),
      event.getFcmToken(),
      event.getMaxTemperature(),
      event.getMinTemperature(),
      event.getSkyCondition(),
      event.getPrecipitationType(),
      recommendedClothes
      
    );
    clothesMatchedEventPublisher.publish(matchedEvent);

  }

   private ClothesItemDto toDto(Clothes clothes){
      return new ClothesItemDto(
        clothes.getClothesId(),
        clothes.getName(),
        clothes.getCategory().name(),
        clothes.getImageUrl()
      );
    }



  public int calAvgTemp(WeatherEvent event){
    if(event.getMaxTemperature()!=null && event.getMinTemperature()!=null){
      return (int) Math.round((event.getMaxTemperature()+event.getMinTemperature())/2.0);
    }else if(event.getMaxTemperature()!=null){
        return event.getMaxTemperature().intValue();
        } else if (event.getMinTemperature() != null) {
            return event.getMinTemperature().intValue();
        }
        return 20; // // 기본값: fallback과 동일하게 20도
  }

}
