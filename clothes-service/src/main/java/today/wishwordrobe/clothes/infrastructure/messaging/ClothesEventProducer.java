package today.wishwordrobe.clothes.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ로 옷장 이벤트를 발행하는 Producer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClothesEventProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 옷장 이벤트 발행
     * @param event 발행할 이벤트
     */
    public void publishEvent(ClothesEvent event) {
        String routingKey = getRoutingKey(event.getEventType());

        log.info("옷장 이벤트 발행: eventType={}, clothesId={}, userId={}, routingKey={}",
                event.getEventType(), event.getClothesId(), event.getUserId(), routingKey);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CLOTHES_EXCHANGE,
                routingKey,
                event
        );

        log.debug("이벤트 발행 완료: {}", event);
    }

    /**
     * 이벤트 타입에 따라 라우팅 키 결정
     */
    private String getRoutingKey(ClothesEvent.EventType eventType) {
        switch (eventType) {
            case CREATED:
                return RabbitMQConfig.CLOTHES_CREATED_KEY;
            case UPDATED:
                return RabbitMQConfig.CLOTHES_UPDATED_KEY;
            case DELETED:
                return RabbitMQConfig.CLOTHES_DELETED_KEY;
            default:
                throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }
}
