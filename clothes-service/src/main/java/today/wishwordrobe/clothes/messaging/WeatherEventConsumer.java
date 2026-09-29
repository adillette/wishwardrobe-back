package today.wishwordrobe.clothes.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import today.wishwordrobe.clothes.application.ClothesService;
import today.wishwordrobe.clothes.domain.ClothesResponse;
import today.wishwordrobe.clothes.messaging.dto.ClothesMatchedEvent;
import today.wishwordrobe.clothes.messaging.dto.WeatherEvent;

/*
spring AMQP



*/


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

    //기온 정보가 없으면 발행하지 않고 종료
    //예외 던지면 Spring AMQP 기본 설정상 메시지가 큐로 재적재되어 무한 재시도하게됨
    List<ClothesResponse> recommended = clothesService.recommendByTemperatures(event.getUserId(),
     event.getMaxTemperature(), 
     event.getMinTemperature(), 
     null);
    List<ClothesItemDto> recommendedClothes = new ArrayList<>();
    for(ClothesResponse r : recommended){
      recommendedClothes.add(toDto(r));
    }

   
    ClothesMatchedEvent matchedEvent= new ClothesMatchedEvent(
      UUID.randomUUID().toString(),
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

   private ClothesItemDto toDto(ClothesResponse r){
    String imageUrl = r.getImageUrl();
     if (imageUrl != null && imageUrl.startsWith("data:")) {
      imageUrl = null;
    }
    
    
    return new ClothesItemDto(
        r.getClothesId(),
        r.getName(),
        r.getCategory().name(),
        r.getImageUrl()
      );
    }



 

}
