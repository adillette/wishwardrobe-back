package today.wishwordrobe.weather.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import today.wishwordrobe.weather.application.WeatherService;
import today.wishwordrobe.weather.dto.WeatherForecastDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * WeatherRequestConsumer#handleWeatherRequest() 단위 테스트.
 * 큐에서 받은 lat/lon으로 WeatherService를 호출하고, 결과를 WeatherEvent로 발행하는지 검증.
 */
@ExtendWith(MockitoExtension.class)
class WeatherRequestConsumerTest {

    @Mock
    WeatherService weatherService;

    @Mock
    WeatherEventPublisher weatherEventPublisher;

    WeatherRequestConsumer consumer;

    @Test
    @DisplayName("날씨 조회 성공 시 결과를 WeatherEvent로 변환해 publish 한다")
    void publishesWeatherEventOnSuccess() {
        consumer = new WeatherRequestConsumer(weatherService, weatherEventPublisher);

        WeatherRequestEvent request = buildRequestEvent(1L, 37.5665, 126.9780);
        WeatherForecastDTO forecast = WeatherForecastDTO.builder()
                .maxTemperature(28.0)
                .minTemperature(18.0)
                .skyCondition("맑음")
                .precipitationType("없음")
                .build();

        when(weatherService.getWeatherByCoordinates(37.5665, 126.9780)).thenReturn(Mono.just(forecast));

        consumer.handleWeatherRequest(request);

        ArgumentCaptor<WeatherEvent> captor = ArgumentCaptor.forClass(WeatherEvent.class);
        verify(weatherEventPublisher).publish(captor.capture());

        WeatherEvent published = captor.getValue();
        assertThat(published.getEventId()).isNotBlank();
        assertThat(published.getUserId()).isEqualTo(1L);
        assertThat(published.getLat()).isEqualTo(37.5665);
        assertThat(published.getLon()).isEqualTo(126.9780);
        assertThat(published.getMaxTemperature()).isEqualTo(28.0);
        assertThat(published.getMinTemperature()).isEqualTo(18.0);
        assertThat(published.getSkyCondition()).isEqualTo("맑음");
        assertThat(published.getPrecipitationType()).isEqualTo("없음");
    }

    @Test
    @DisplayName("날씨 조회 실패 시 publish를 호출하지 않는다")
    void doesNotPublishOnFailure() {
        consumer = new WeatherRequestConsumer(weatherService, weatherEventPublisher);

        WeatherRequestEvent request = buildRequestEvent(1L, 37.5665, 126.9780);
        when(weatherService.getWeatherByCoordinates(anyDouble(), anyDouble()))
                .thenReturn(Mono.error(new RuntimeException("기상청 API 호출 실패")));

        consumer.handleWeatherRequest(request);

        verify(weatherEventPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }

    private WeatherRequestEvent buildRequestEvent(long userId, double lat, double lon) {
        WeatherRequestEvent event = new WeatherRequestEvent();
        ReflectionTestUtils.setField(event, "userId", userId);
        ReflectionTestUtils.setField(event, "lat", lat);
        ReflectionTestUtils.setField(event, "lon", lon);
        return event;
    }
}
