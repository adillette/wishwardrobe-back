package today.wishwordrobe.clothes.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import today.wishwordrobe.clothes.application.ClothesService;
import today.wishwordrobe.clothes.domain.ClothesResponse;
import today.wishwordrobe.clothes.domain.ClothingCategory;
import today.wishwordrobe.clothes.messaging.dto.ClothesMatchedEvent;
import today.wishwordrobe.clothes.messaging.dto.WeatherEvent;

@ExtendWith(MockitoExtension.class)
public class WeatherEventConsumerTest {
    @Mock
    private ClothesService clothesService;

    @Mock
    private ClothesMatchedEventPublisher clothesMatchedEventPublisher;

    @InjectMocks
    private WeatherEventConsumer weatherEventConsumer;

    @Test
    void 정상_이벤트_수신시_추천결과_발행() {
        // given
        WeatherEvent event = new WeatherEvent();
        event.setUserId(1L);
        event.setMaxTemperature(25.0);
        event.setMinTemperature(15.0);
        event.setSkyCondition("맑음");
        event.setFcmToken("test-fcm-token");

        ClothesResponse response = new ClothesResponse(
                1L, "흰 티셔츠", ClothingCategory.TOP, "http://test.com/image.png");

        when(clothesService.recommendByTemperatures(1L, 25.0, 15.0, null))
                .thenReturn(List.of(response));

        // when
        weatherEventConsumer.handleWeatherEvent(event);

        // then
        verify(clothesMatchedEventPublisher, times(1))
                .publish(any(ClothesMatchedEvent.class));
    }

    @Test
    void 최고기온만_있을때_최저기온_null로_전달() {
        // given
        WeatherEvent event = new WeatherEvent();
        event.setUserId(1L);
        event.setMaxTemperature(25.0);
        event.setMinTemperature(null);
        event.setFcmToken("test-fcm-token");

        when(clothesService.recommendByTemperatures(eq(1L), eq(25.0), isNull(), isNull()))
                .thenReturn(List.of());

        // when
        weatherEventConsumer.handleWeatherEvent(event);

        // then
        verify(clothesMatchedEventPublisher, times(1))
                .publish(any(ClothesMatchedEvent.class));
    }

    @Test
    void 기온_둘다_null이면_예외발생하고_발행되지않음() {
        // given
        WeatherEvent event = new WeatherEvent();
        event.setUserId(1L);
        event.setMaxTemperature(null);
        event.setMinTemperature(null);
        event.setFcmToken("test-fcm-token");

        when(clothesService.recommendByTemperatures(eq(1L), isNull(), isNull(), isNull()))
                .thenThrow(new IllegalStateException("Temperature data is missing"));

        // when & then
        assertThrows(IllegalStateException.class,
                () -> weatherEventConsumer.handleWeatherEvent(event));

        verify(clothesMatchedEventPublisher, never()).publish(any(ClothesMatchedEvent.class));
    }
}
