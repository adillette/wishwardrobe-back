package today.wishwordrobe.clothes.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import today.wishwordrobe.clothes.messaging.dto.ClothesMatchedEvent;
//RabbitMQ Exchange에 메시지를 던진다.
@Slf4j
@Component
@RequiredArgsConstructor
public class ClothesMatchedEventPublisher {
  private final RabbitTemplate rabbitTemplate;

  public void publish(ClothesMatchedEvent event){
    rabbitTemplate.convertAndSend(
      RabbitMQConfig.CLOTHES_EXCHANGE,
      RabbitMQConfig.CLOTHES_ROUTING_KEY,
      event
    );
    //clothes.exchange로 clothes.matched routing key와 함께 발행
    //notification-service가 clothes.notification.queu 구독중이면 수신
     log.info("ClothesMatchedEvent 발행 완료. userId={}", event.getUserId());
  }

}
