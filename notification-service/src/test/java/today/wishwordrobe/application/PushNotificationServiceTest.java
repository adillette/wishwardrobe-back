package today.wishwordrobe.application;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import today.wishwordrobe.firebase.FCMService;
import today.wishwordrobe.infrastructure.FcmTokenRepository;
import today.wishwordrobe.infrastructure.WebPushSubscriptionRepository;
import today.wishwordrobe.webpush.WebPushSendSummary;
import today.wishwordrobe.webpush.WebPushService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * PushNotificationService#sendToUserWithChannelCheck 단위 테스트.
 * FCM/WebPush 채널 유무 조합과 전송 결과를 격리 검증.
 */
@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock FCMService fcmService;
    @Mock FcmTokenRepository fcmTokenRepository;
    @Mock WebPushSubscriptionRepository webPushSubscriptionRepository;
    @Mock WebPushService webPushService;

    PushNotificationService service;

    private static final Long USER_ID = 123L;
    private static final String TITLE = "오늘의 날씨 맑음 | 맑음";
    private static final String BODY = "최고 28°C / 최저 18°C\n추천 옷: 반팔";

    @BeforeEach
    void setUp() {
        service = new PushNotificationService(
                new SimpleMeterRegistry(),
                fcmService,
                fcmTokenRepository,
                webPushSubscriptionRepository,
                webPushService);
        service.initMetrics();
    }

    // ─── FCM만 존재 ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("FCM만 존재하는 경우")
    class FcmOnly {

        @Test
        @DisplayName("FCM 전송 성공 → SENT 반환")
        void fcm_success() {
            when(fcmService.hasActiveToken(USER_ID)).thenReturn(Mono.just(true));
            when(webPushService.hasActiveSubscription(USER_ID)).thenReturn(Mono.just(false));
            when(fcmService.sendMessageToUser(eq(USER_ID), anyString(), anyString()))
                    .thenReturn(Flux.just("msg-id-001"));

            StepVerifier.create(service.sendToUserWithChannelCheck(USER_ID, TITLE, BODY))
                    .expectNext(ChannelSendResult.SENT)
                    .verifyComplete();
        }

        @Test
        @DisplayName("FCM 전송 실패(빈 Flux) → FAILED 반환")
        void fcm_failure() {
            when(fcmService.hasActiveToken(USER_ID)).thenReturn(Mono.just(true));
            when(webPushService.hasActiveSubscription(USER_ID)).thenReturn(Mono.just(false));
            when(fcmService.sendMessageToUser(eq(USER_ID), anyString(), anyString()))
                    .thenReturn(Flux.error(new RuntimeException("FCM 연결 실패")));

            StepVerifier.create(service.sendToUserWithChannelCheck(USER_ID, TITLE, BODY))
                    .expectNext(ChannelSendResult.FAILED)
                    .verifyComplete();
        }
    }

    // ─── WebPush만 존재 ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("WebPush만 존재하는 경우")
    class WebPushOnly {

        @Test
        @DisplayName("WebPush 전송 성공(success>0) → SENT 반환")
        void webpush_success() {
            when(fcmService.hasActiveToken(USER_ID)).thenReturn(Mono.just(false));
            when(webPushService.hasActiveSubscription(USER_ID)).thenReturn(Mono.just(true));
            when(webPushService.sendToUser(eq(USER_ID), any()))
                    .thenReturn(Mono.just(new WebPushSendSummary(1, 0, 0)));

            StepVerifier.create(service.sendToUserWithChannelCheck(USER_ID, TITLE, BODY))
                    .expectNext(ChannelSendResult.SENT)
                    .verifyComplete();
        }

        @Test
        @DisplayName("WebPush 만료 구독(expired>0) → SENT 반환")
        void webpush_expired() {
            when(fcmService.hasActiveToken(USER_ID)).thenReturn(Mono.just(false));
            when(webPushService.hasActiveSubscription(USER_ID)).thenReturn(Mono.just(true));
            when(webPushService.sendToUser(eq(USER_ID), any()))
                    .thenReturn(Mono.just(new WebPushSendSummary(0, 1, 0)));

            StepVerifier.create(service.sendToUserWithChannelCheck(USER_ID, TITLE, BODY))
                    .expectNext(ChannelSendResult.SENT)
                    .verifyComplete();
        }

        @Test
        @DisplayName("WebPush 전송 실패(success=0, expired=0) → FAILED 반환")
        void webpush_all_failed() {
            when(fcmService.hasActiveToken(USER_ID)).thenReturn(Mono.just(false));
            when(webPushService.hasActiveSubscription(USER_ID)).thenReturn(Mono.just(true));
            when(webPushService.sendToUser(eq(USER_ID), any()))
                    .thenReturn(Mono.just(new WebPushSendSummary(0, 0, 1)));

            StepVerifier.create(service.sendToUserWithChannelCheck(USER_ID, TITLE, BODY))
                    .expectNext(ChannelSendResult.FAILED)
                    .verifyComplete();
        }

        @Test
        @DisplayName("WebPush 예외 → FAILED 반환")
        void webpush_exception() {
            when(fcmService.hasActiveToken(USER_ID)).thenReturn(Mono.just(false));
            when(webPushService.hasActiveSubscription(USER_ID)).thenReturn(Mono.just(true));
            when(webPushService.sendToUser(eq(USER_ID), any()))
                    .thenReturn(Mono.error(new RuntimeException("WebPush 서버 오류")));

            StepVerifier.create(service.sendToUserWithChannelCheck(USER_ID, TITLE, BODY))
                    .expectNext(ChannelSendResult.FAILED)
                    .verifyComplete();
        }
    }

    // ─── FCM + WebPush 둘 다 존재 ─────────────────────────────────────────────

    @Nested
    @DisplayName("FCM + WebPush 둘 다 존재하는 경우")
    class BothChannels {

        @Test
        @DisplayName("둘 다 성공 → SENT 반환")
        void both_success() {
            when(fcmService.hasActiveToken(USER_ID)).thenReturn(Mono.just(true));
            when(webPushService.hasActiveSubscription(USER_ID)).thenReturn(Mono.just(true));
            when(fcmService.sendMessageToUser(eq(USER_ID), anyString(), anyString()))
                    .thenReturn(Flux.just("msg-id-002"));
            when(webPushService.sendToUser(eq(USER_ID), any()))
                    .thenReturn(Mono.just(new WebPushSendSummary(1, 0, 0)));

            StepVerifier.create(service.sendToUserWithChannelCheck(USER_ID, TITLE, BODY))
                    .expectNext(ChannelSendResult.SENT)
                    .verifyComplete();
        }

        @Test
        @DisplayName("FCM 실패 + WebPush 성공 → SENT 반환 (하나라도 성공이면 SENT)")
        void fcm_fail_webpush_ok() {
            when(fcmService.hasActiveToken(USER_ID)).thenReturn(Mono.just(true));
            when(webPushService.hasActiveSubscription(USER_ID)).thenReturn(Mono.just(true));
            when(fcmService.sendMessageToUser(eq(USER_ID), anyString(), anyString()))
                    .thenReturn(Flux.error(new RuntimeException("FCM 오류")));
            when(webPushService.sendToUser(eq(USER_ID), any()))
                    .thenReturn(Mono.just(new WebPushSendSummary(1, 0, 0)));

            StepVerifier.create(service.sendToUserWithChannelCheck(USER_ID, TITLE, BODY))
                    .expectNext(ChannelSendResult.SENT)
                    .verifyComplete();
        }

        @Test
        @DisplayName("FCM 성공 + WebPush 실패 → SENT 반환")
        void fcm_ok_webpush_fail() {
            when(fcmService.hasActiveToken(USER_ID)).thenReturn(Mono.just(true));
            when(webPushService.hasActiveSubscription(USER_ID)).thenReturn(Mono.just(true));
            when(fcmService.sendMessageToUser(eq(USER_ID), anyString(), anyString()))
                    .thenReturn(Flux.just("msg-id-003"));
            when(webPushService.sendToUser(eq(USER_ID), any()))
                    .thenReturn(Mono.just(new WebPushSendSummary(0, 0, 1)));

            StepVerifier.create(service.sendToUserWithChannelCheck(USER_ID, TITLE, BODY))
                    .expectNext(ChannelSendResult.SENT)
                    .verifyComplete();
        }

        @Test
        @DisplayName("둘 다 실패 → FAILED 반환")
        void both_fail() {
            when(fcmService.hasActiveToken(USER_ID)).thenReturn(Mono.just(true));
            when(webPushService.hasActiveSubscription(USER_ID)).thenReturn(Mono.just(true));
            when(fcmService.sendMessageToUser(eq(USER_ID), anyString(), anyString()))
                    .thenReturn(Flux.error(new RuntimeException("FCM 오류")));
            when(webPushService.sendToUser(eq(USER_ID), any()))
                    .thenReturn(Mono.just(new WebPushSendSummary(0, 0, 1)));

            StepVerifier.create(service.sendToUserWithChannelCheck(USER_ID, TITLE, BODY))
                    .expectNext(ChannelSendResult.FAILED)
                    .verifyComplete();
        }
    }

    // ─── 채널 없음 ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("채널 없음")
    class NoChannel {

        @Test
        @DisplayName("FCM, WebPush 모두 없음 → NO_CHANNEL 반환")
        void no_channel() {
            when(fcmService.hasActiveToken(USER_ID)).thenReturn(Mono.just(false));
            when(webPushService.hasActiveSubscription(USER_ID)).thenReturn(Mono.just(false));

            StepVerifier.create(service.sendToUserWithChannelCheck(USER_ID, TITLE, BODY))
                    .expectNext(ChannelSendResult.NO_CHANNEL)
                    .verifyComplete();
        }
    }
}
