package today.wishwordrobe.clothes.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import today.wishwordrobe.clothes.application.ClothesService;
import today.wishwordrobe.clothes.domain.Clothes;
import today.wishwordrobe.clothes.domain.ClothingCategory;
import today.wishwordrobe.clothes.domain.TempRange;
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
        event.setUserId("1");
        event.setMaxTemperature(25.0);
        event.setMinTemperature(15.0);  // // avgTemp = 20 → TempRange.MILD 또는 해당 범위
        event.setSkyCondition("맑음");
        event.setFcmToken("test-fcm-token");

        Clothes clothes = new Clothes();
        clothes.setClothesId(1L);
        clothes.setName("흰 티셔츠");
        clothes.setCategory(ClothingCategory.TOP);
        clothes.setImageUrl("http://test.com/image.png");
        clothes.setCreatedAt(LocalDateTime.now());

        // // avgTemp=20이면 TempRange.fromTemperature(20) 결과값으로 맞춰야 함
        TempRange expectedRange = TempRange.fromTemperature(20);

        when(clothesService.getClothesWithCache(1L, expectedRange, null))
                .thenReturn(List.of(clothes));

        // when
        weatherEventConsumer.handleWeatherEvent(event);

        // then
        verify(clothesMatchedEventPublisher, times(1))
                .publish(any(ClothesMatchedEvent.class));
    }

    @Test
    void 최고기온만_있을때_fallback_처리() {
        // given
        WeatherEvent event = new WeatherEvent();
        event.setUserId("1");
        event.setMaxTemperature(25.0);
        event.setMinTemperature(null);  // // null 케이스
        event.setFcmToken("test-fcm-token");

        TempRange expectedRange = TempRange.fromTemperature(25);
        when(clothesService.getClothesWithCache(1L, expectedRange, null))
                .thenReturn(List.of());

        // when
        weatherEventConsumer.handleWeatherEvent(event);

        // then
        verify(clothesMatchedEventPublisher, times(1))
                .publish(any(ClothesMatchedEvent.class));
    }

    @Test
    void 기온_둘다_null이면_기본값20으로_처리() {
        // given
        WeatherEvent event = new WeatherEvent();
        event.setUserId("1");
        event.setMaxTemperature(null);
        event.setMinTemperature(null);
        event.setFcmToken("test-fcm-token");

        TempRange expectedRange = TempRange.fromTemperature(20);  // // fallback 20도
        when(clothesService.getClothesWithCache(1L, expectedRange, null))
                .thenReturn(List.of());

        // when
        weatherEventConsumer.handleWeatherEvent(event);

        // then
        verify(clothesMatchedEventPublisher, times(1))
                .publish(any(ClothesMatchedEvent.class));
    }
}