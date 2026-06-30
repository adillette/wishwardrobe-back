package today.wishwordrobe.outbox.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import today.wishwordrobe.application.PushNotificationService;
import today.wishwordrobe.messaging.ClothesMatchedEvent;
import today.wishwordrobe.messaging.NotificationMessageBuilder;
import today.wishwordrobe.outbox.domain.OutboxEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

  private final OutboxEventService outboxEventService;
  private final PushNotificationService pushNotificationService;
  // private final FCMService fcmService;
  // private final WebPushService webPushService;
  private final NotificationMessageBuilder messageBuilder;
  
  private final ObjectMapper objectMapper;

  //재처리 스케줄러 1분 마다 실행
  //PENDING 상태인 미처리 건 조회-> fcm/ WEBPUSH 재전송 시도
  //서버 재시작 후 미처리건 자동 복구
  @Scheduled(fixedDelay = 60000)
  public void processPendingEvents(){
    log.info("OutboxProcessor 실행-PENDING 건 조회 시작");
     outboxEventService.getPendingEvents()
                .flatMap(this::retryNotification)
                        .subscribe(
                        result -> log.debug("재처리 완료. eventId={}", result.getEventId()),
                        e -> log.error("재처리 중 오류 발생: {}", e.getMessage()));
    }


//정리 스케줄러 매일 새벽 3시 실행
@Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
public void cleanupSentEvents(){
  log.info("OutboxProcessor 실행 - PENDING 건 조회 시작");
  outboxEventService.cleanupOldSentEvent()
      .subscribe(
        count->log.info("정리 완료 삭제 건수={}",count),
        error->log.error("cleanup 실패:{}", error.getMessage())
      );
}


  //retry
  private Mono<OutboxEvent> retryNotification(OutboxEvent event){
     log.info("재처리 시도. eventId={}, userId={}, retryCount={}",
                event.getEventId(), event.getUserId(), event.getRetryCount());

    try {
      ClothesMatchedEvent clothesMatchedEvent = objectMapper.readValue(event.getPayload(), ClothesMatchedEvent.class);
      String title=messageBuilder.buildTitle(clothesMatchedEvent);
      String body= messageBuilder.buildBody(clothesMatchedEvent);
      
      return pushNotificationService.sendToUserWithChannelCheck(event.getUserId(), title, body)
        .flatMap(results->switch (results) {
          case SENT -> outboxEventService.markAsSent(event);
          case NO_CHANNEL -> outboxEventService.markAsNoChannel(event, "등록된 채널 없음");
          case FAILED -> outboxEventService.markAsFailed(event, "전송 실패");
        })
        .onErrorResume(e->{
          log.error("재처리 전송 실패 eventId={}, error={}", event.getEventId(),e.getMessage());
          return outboxEventService.markAsFailed(event, e.getMessage());
        });


    } catch (Exception e) {
      log.error("payload 역직렬화 실패 eventId={}", event.getEventId(),e);
      return outboxEventService.markAsFailed(event, "payload 파싱 실패: "+e.getMessage());
    }
  }
}
