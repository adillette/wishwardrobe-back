package today.wishwordrobe.weather.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherEventPublisher {
  private final RabbitTemplate rabbitTemplate;

  //publish: weatherevent를 weather.exchange로 발행
  /* 순서 
  RabbitTemplate의 convertand send 로 weatherEvent를 jackson 컨버터로 json 만들어
  weather.exchange는 weather.fetch 라우팅 키를 weather.clothes.queue에 보내
  clothes-service의 consumer가 수신한다.
   */
  public void publish(WeatherEvent event){
    rabbitTemplate.convertAndSend(
      RabbitMQConfig.WEATHER_EXCHANGE,
      RabbitMQConfig.WEATHER_ROUTING_KEY,
      event
    );

    log.info("weatherevent 발행완료  eventId={}, userId={}",
                event.getEventId(), event.getUserId());
  }

}
