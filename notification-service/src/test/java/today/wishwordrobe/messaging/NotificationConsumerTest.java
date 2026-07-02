package today.wishwordrobe.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import today.wishwordrobe.application.ChannelSendResult;
import today.wishwordrobe.application.PushNotificationService;
import today.wishwordrobe.outbox.application.OutboxEventService;
import today.wishwordrobe.outbox.domain.OutboxEvent;
import today.wishwordrobe.outbox.domain.OutboxStatus;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RabbitMQ에서 ClothesMatchedEvent를 수신한 뒤
 * Outbox 저장 → 알림 전송 → 상태 갱신 흐름을 격리 테스트.
 *
 * 실제 RabbitMQ/MongoDB/Redis 없이 Mockito로만 동작.
 */
@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock OutboxEventService outboxEventService;
    @Mock PushNotificationService pushNotificationService;
    @Mock NotificationMessageBuilder messageBuilder;

    @InjectMocks NotificationConsumer consumer;

    private OutboxEvent pendingOutbox;

    @BeforeEach
    void setUp() {
        pendingOutbox = OutboxEvent.builder()
                .eventId("evt-001")
                .userId("123")
                .eventType("CLOTHES_MATCHED")
                .payload("{}")
                .build();
    }

    // ─── 정상 전송 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("이벤트 수신 → Outbox 저장 → 알림 전송 성공 → SENT 마킹")
    void handle_success_sent() {
        OutboxEvent sentOutbox = pendingOutbox;
        sentOutbox.setStatus(OutboxStatus.SENT);

        when(outboxEventService.saveWithLock(any(), any(), any(), any()))
                .thenReturn(Mono.just(pendingOutbox));
        when(messageBuilder.buildTitle(any())).thenReturn("오늘의 날씨 맑음 | 맑음");
        when(messageBuilder.buildBody(any())).thenReturn("최고 28°C / 최저 18°C\n추천 옷: 반팔");
        when(pushNotificationService.sendToUserWithChannelCheck(any(), any(), any()))
                .thenReturn(Mono.just(ChannelSendResult.SENT));
        when(outboxEventService.markAsSent(any())).thenReturn(Mono.just(sentOutbox));

        consumer.handleClothesMatchedEvent(sampleEvent());

        // subscribe() 내부가 비동기이므로 짧게 대기 후 검증
        awaitAsync();

        verify(outboxEventService).saveWithLock(eq("evt-001"), eq("123"),
                eq("CLOTHES_MATCHED"), any());
        verify(pushNotificationService).sendToUserWithChannelCheck(
                eq("123"), anyString(), anyString());
        verify(outboxEventService).markAsSent(pendingOutbox);
    }

    @Test
    @DisplayName("전송 채널 없음 → NO_CHANNEL 마킹")
    void handle_no_channel() {
        OutboxEvent noChannelOutbox = pendingOutbox;
        noChannelOutbox.setStatus(OutboxStatus.NO_CHANNEL);

        when(outboxEventService.saveWithLock(any(), any(), any(), any()))
                .thenReturn(Mono.just(pendingOutbox));
        when(messageBuilder.buildTitle(any())).thenReturn("제목");
        when(messageBuilder.buildBody(any())).thenReturn("본문");
        when(pushNotificationService.sendToUserWithChannelCheck(any(), any(), any()))
                .thenReturn(Mono.just(ChannelSendResult.NO_CHANNEL));
        when(outboxEventService.markAsNoChannel(any(), any())).thenReturn(Mono.just(noChannelOutbox));

        consumer.handleClothesMatchedEvent(sampleEvent());
        awaitAsync();

        verify(outboxEventService).markAsNoChannel(eq(pendingOutbox), anyString());
        verify(outboxEventService, never()).markAsSent(any());
    }

    @Test
    @DisplayName("전송 실패 → FAILED 마킹")
    void handle_send_failed() {
        OutboxEvent failedOutbox = pendingOutbox;
        failedOutbox.setStatus(OutboxStatus.FAILED);

        when(outboxEventService.saveWithLock(any(), any(), any(), any()))
                .thenReturn(Mono.just(pendingOutbox));
        when(messageBuilder.buildTitle(any())).thenReturn("제목");
        when(messageBuilder.buildBody(any())).thenReturn("본문");
        when(pushNotificationService.sendToUserWithChannelCheck(any(), any(), any()))
                .thenReturn(Mono.just(ChannelSendResult.FAILED));
        when(outboxEventService.markAsFailed(any(), any())).thenReturn(Mono.just(failedOutbox));

        consumer.handleClothesMatchedEvent(sampleEvent());
        awaitAsync();

        verify(outboxEventService).markAsFailed(eq(pendingOutbox), anyString());
        verify(outboxEventService, never()).markAsSent(any());
    }

    @Test
    @DisplayName("Outbox 저장 결과 empty(중복 이벤트) → 알림 전송 스킵")
    void handle_duplicate_event_skipped() {
        // saveWithLock이 Mono.empty()를 반환 → 중복 이벤트로 차단된 상황
        when(outboxEventService.saveWithLock(any(), any(), any(), any()))
                .thenReturn(Mono.empty());

        consumer.handleClothesMatchedEvent(sampleEvent());
        awaitAsync();

        verify(pushNotificationService, never()).sendToUserWithChannelCheck(any(), any(), any());
    }

    @Test
    @DisplayName("알림 전송 중 예외 → FAILED 마킹")
    void handle_exception_during_send() {
        when(outboxEventService.saveWithLock(any(), any(), any(), any()))
                .thenReturn(Mono.just(pendingOutbox));
        when(messageBuilder.buildTitle(any())).thenReturn("제목");
        when(messageBuilder.buildBody(any())).thenReturn("본문");
        when(pushNotificationService.sendToUserWithChannelCheck(any(), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("FCM 연결 실패")));
        when(outboxEventService.markAsFailed(any(), any())).thenReturn(Mono.just(pendingOutbox));

        consumer.handleClothesMatchedEvent(sampleEvent());
        awaitAsync();

        verify(outboxEventService).markAsFailed(eq(pendingOutbox), contains("FCM 연결 실패"));
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private ClothesMatchedEvent sampleEvent() {
        ClothesMatchedEvent event = new ClothesMatchedEvent();
        event.setEventId("evt-001");
        event.setUserId(123L);
        event.setFcmToken("dummy-fcm-token");
        event.setMaxTemperature(28.0);
        event.setMinTemperature(18.0);
        event.setSkyCondition("맑음");
        event.setPrecipitationType("없음");

        ClothesItemDto item = new ClothesItemDto();
        item.setClothesId(1L);
        item.setName("반팔");
        item.setCategory("상의");
        event.setRecommendedClothes(List.of(item));

        return event;
    }

    /** consumer 내부의 subscribe()가 비동기이므로 검증 전 잠깐 대기 */
    private void awaitAsync() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
