package today.wishwordrobe.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherRequestPublisher {
  private final RabbitTemplate rabbitTemplate;

  public void publish(WeatherRequestEvent event){
  
    rabbitTemplate.convertAndSend(RabbitMQConfig.WEATHER_REQUEST_EXCHANGE,
      RabbitMQConfig.WEATHER_REQUEST_ROUTING_KEY,
      event
    );
    log.info("WeatherRequestEvent publish 완료 - userId: {}, lat: {}, lon: {}",
                event.getUserId(), event.getLat(), event.getLon());

  }
}
