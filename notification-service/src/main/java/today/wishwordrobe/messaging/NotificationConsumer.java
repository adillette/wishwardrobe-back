package today.wishwordrobe.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import today.wishwordrobe.application.PushNotificationService;
import today.wishwordrobe.outbox.application.OutboxEventService;
import today.wishwordrobe.outbox.domain.OutboxEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {
  private final OutboxEventService outboxEventService;
  
  private final PushNotificationService pushNotificationService;
  // private final FCMService fcmService;
  // private final WebPushService webPushService;
  private final NotificationMessageBuilder messageBuilder;
  // webPushservice.sendToUser()에 위임 FCMService패턴과 동일
  private static final String EVENT_TYPE = "CLOTHES_MATCHED";

  // Spring이 Json -> ClothesMathedEvent 객체로 역직렬화
  // clothes-service가 clothes.exchange → clothes.matched 로 발행한 것을 여기서 수신
  @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
  public void handleClothesMatchedEvent(ClothesMatchedEvent event) {
    outboxEventService.saveWithLock(
        event.getEventId(),
        event.getUserId(),
        EVENT_TYPE,
        event)
        .flatMap(outboxEvent -> sendNotification(event, outboxEvent))
        // subscribe(): Reac
        .subscribe(
            result -> log.info("알림 처리 완료 .eventId={}", event.getEventId()),
            error -> log.error("알림 처리 실패 .eventId={},error={}",
                event.getEventId(),
                error.getMessage()));
  }

  // FCM+ WebPush 병렬 전송 후 Outbox 상태 업데이트
  // Mono.zip FCM과 WebPush를 병렬로 실행
  // 둘다 완료되면 markAsSent() 호출
  // 하나라도 실패하면 onErrorResume ->markAsFailed()호출
  private Mono<OutboxEvent> sendNotification(ClothesMatchedEvent event,
      OutboxEvent outboxEvent) {
    String title = messageBuilder.buildTitle(event);
    String body = messageBuilder.buildBody(event);
  
    // // Mono<WebPushSendSummary> → Mono<Void>
    return pushNotificationService.sendToUserWithChannelCheck(event.getUserId(), title, body)
        .flatMap(result -> switch (result) {
          case SENT ->outboxEventService.markAsSent(outboxEvent);
          case NO_CHANNEL->outboxEventService.markAsNoChannel(outboxEvent, "등록된 채널 없음");
          case FAILED ->outboxEventService.markAsFailed(outboxEvent, "전송 실패");
        })
        .onErrorResume(e->{
          log.error("전송 실패 eventId={},error={}", outboxEvent.getEventId(),e.getMessage());
        return outboxEventService.markAsFailed(outboxEvent, e.getMessage());
        });
  }

}
