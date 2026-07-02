package today.wishwordrobe.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import today.wishwordrobe.messaging.WeatherRequestEvent;
import today.wishwordrobe.messaging.WeatherRequestPublisher;
import today.wishwordrobe.presentation.dto.UserLocationResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DailyWeatherScheduler#run() 단위 테스트.
 * 활성 사용자 위치를 조회해 사용자별로 WeatherRequestEvent를 publish 하는지 검증.
 */
@ExtendWith(MockitoExtension.class)
class DailyWeatherSchedulerTest {

    @Mock
    UserLocationService userLocationService;

    @Mock
    WeatherRequestPublisher weatherRequestPublisher;

    @Mock
    org.asynchttpclient.AsyncHttpClient asyncHttpClient;

    DailyWeatherScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new DailyWeatherScheduler(userLocationService, weatherRequestPublisher, asyncHttpClient);
        ReflectionTestUtils.setField(scheduler, "enabled", true);
    }

    @Test
    @DisplayName("활성 사용자 각각에 대해 WeatherRequestEvent를 publish 한다")
    void publishesEventForEachActiveUser() {
        UserLocationResponse user1 = UserLocationResponse.builder()
                .userId(1L).lat(37.5).lon(127.0).enabled(true).build();
        UserLocationResponse user2 = UserLocationResponse.builder()
                .userId(2L).lat(35.1).lon(129.0).enabled(true).build();

        when(userLocationService.getActiveUserLocations()).thenReturn(Flux.just(user1, user2));

        scheduler.run();

        verify(weatherRequestPublisher, times(1)).publish(argThatMatches(1L, 37.5, 127.0));
        verify(weatherRequestPublisher, times(1)).publish(argThatMatches(2L, 35.1, 129.0));
    }

    @Test
    @DisplayName("활성 사용자가 없으면 publish를 호출하지 않는다")
    void noPublishWhenNoActiveUsers() {
        when(userLocationService.getActiveUserLocations()).thenReturn(Flux.empty());

        scheduler.run();

        verify(weatherRequestPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("enabled=false 이면 사용자 조회 자체를 하지 않는다")
    void skipsWhenDisabled() {
        ReflectionTestUtils.setField(scheduler, "enabled", false);

        scheduler.run();

        verify(userLocationService, never()).getActiveUserLocations();
        verify(weatherRequestPublisher, never()).publish(any());
    }

    private WeatherRequestEvent argThatMatches(long userId, double lat, double lon) {
        return org.mockito.ArgumentMatchers.argThat(event ->
                event.getUserId() == userId && event.getLat() == lat && event.getLon() == lon);
    }
}
