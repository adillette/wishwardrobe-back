package today.wishwordrobe.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * WeatherRequestPublisher#publish() 단위 테스트.
 * weather.request.exchange로 올바른 라우팅 키와 이벤트가 전송되는지 검증.
 */
@ExtendWith(MockitoExtension.class)
class WeatherRequestPublisherTest {

    @Mock
    RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("WEATHER_REQUEST_EXCHANGE/ROUTING_KEY로 이벤트를 전송한다")
    void publishesToWeatherRequestExchange() {
        WeatherRequestPublisher publisher = new WeatherRequestPublisher(rabbitTemplate);
        WeatherRequestEvent event = WeatherRequestEvent.builder()
                .userId(1L)
                .lat(37.5665)
                .lon(126.9780)
                .build();

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.WEATHER_REQUEST_EXCHANGE),
                eq(RabbitMQConfig.WEATHER_REQUEST_ROUTING_KEY),
                eq(event));
    }
}
