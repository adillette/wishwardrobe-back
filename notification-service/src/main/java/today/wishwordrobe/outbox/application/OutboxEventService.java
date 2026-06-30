package today.wishwordrobe.outbox.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import today.wishwordrobe.outbox.domain.OutboxEvent;
import today.wishwordrobe.outbox.domain.OutboxStatus;
import today.wishwordrobe.outbox.infrastructure.OutboxEventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventService {
   private final OutboxEventRepository outboxEventRepository;
    private final DistributedLockService distributedLockService;
    private final ObjectMapper objectMapper;

   private static final int MAX_RETRY = 3;

   //[핵심] saveWithLock: 이중 방어 구조
    //
    // 1차 방어 - Redis Striped Lock:
    // userId + date + eventId 키로 락 시도
    // 같은 userId의 동시 처리 시도 자체를 차단
    // 다른 userId는 완전 독립 → Lock Bottleneck 없음
    //
    // 2차 방어 - MongoDB Unique Index:
    // 분산락 통과해도 DB 레벨 atomic 보장
    // Redis 장애 같은 극단적 상황에서도 중복 저장 불가

  public Mono<OutboxEvent> saveWithLock(String eventId, String userId, String eventType, Object payload){
    return Mono.fromCallable(()->objectMapper.writeValueAsString(payload))
    .onErrorMap(JsonProcessingException.class,
      e->new RuntimeException("payload 직렬화 실패: "+e.getMessage())
    ).flatMap(payloadJson->{
      OutboxEvent outboxEvent= OutboxEvent.builder()
      .eventId(eventId)
      .userId(userId)
      .eventType(eventType)
      .payload(payloadJson)
      .build();
      return distributedLockService.tryLock(userId, eventId)
        .flatMap(lockValue->{
          if(lockValue==null){
            log.warn("중복 요청 차단. userId={}, eventId={}",
                                        userId, eventId);
                                return Mono.<OutboxEvent>empty();
          }
          return outboxEventRepository.save(outboxEvent)
          .doOnSuccess(e->log.info("outbox 저장 완료. eventId={},userId={}",eventId,userId))
          .onErrorResume(DuplicateKeyException.class,e->{
            log.warn("중복이벤트 차단(DB Unique Index).eventId={}",eventId);
         return Mono.empty();
          })
          .doFinally(signal->distributedLockService.releaseLock(userId, eventId, lockValue)
        .subscribe());

        });
    });


    
  }

//markAsSent: FCM/ WebPush 전송 성공 처리
//pending -> sent processedAt 기록
//processedAt이 있어야 cleanup 스케줄러가 오래된 건 삭제 가능
  public Mono<OutboxEvent> markAsSent(OutboxEvent event){
    event.setStatus(OutboxStatus.SENT);
    event.setProcessedAt(LocalDateTime.now());

    return outboxEventRepository.save(event)
            .doOnSuccess(e->log.info("Outbox SETN.eventId={}, userId={}",event.getEventId(),event.getUserId()));
  }

  //MarkAsFailed: FCM/WebPush 전송 실패 처리
  //pending 상태 retryCount max_retry 3회 미만
  //초과시 Failed 마킹
  // failureReason 저장 -> 실패 원인 추적 가능
  public Mono<OutboxEvent> markAsFailed(OutboxEvent event,String reason){
    event.setRetryCount(event.getRetryCount());
    event.setFailureReason(reason);
    if(event.getRetryCount()>=MAX_RETRY){
      event.setStatus(OutboxStatus.FAILED);
      log.error("최대 재시도 초과, eventId={}, userId={},retryCount={}",event.getEventId(),event.getUserId(),event.getRetryCount());
    }
    return outboxEventRepository.save(event);
  }

  public Mono<OutboxEvent> markAsNoChannel(OutboxEvent event,String reason){
    event.setStatus(OutboxStatus.NO_CHANNEL);
    event.setFailureReason(reason);
    log.warn("전송 채널 없음. eventId={}, userId={}", event.getEventId(), event.getUserId());
    return outboxEventRepository.save(event);
  }

  //OutbosProcessor: 재처리 대상 조회 
  //Pending 상태+ retryCount 가 max보다 작음
  //서버 재시작 후 미처리 건 자동 재처리
  public Flux<OutboxEvent> getPendingEvents(){
    return outboxEventRepository
            .findByStatusAndRetryCountLessThan(OutboxStatus.PENDING, MAX_RETRY)
            .doOnNext(e->log.info("재처리대상 eventId={}, retryCount={}",
            e.getEventId(),e.getRetryCount()));
          }

  //cleanupOldSentEvents: 7일 이상 지난 sent 건 삭제
  //outbox_events 컬렉션 비대화 방지
  //OutboxProcessor 정리 스케줄러에서 매일 1회 호출
  public Mono<Long> cleanupOldSentEvent(){
    LocalDateTime threshold= LocalDateTime.now().minusDays(7);
    return outboxEventRepository
            .findByStatusAndProcessedAtBefore(OutboxStatus.SENT, threshold)
            .flatMap(event-> outboxEventRepository.delete(event).thenReturn(1L))
            .count()
            .doOnSuccess(count->log.info("Outbox cleanup 완료 삭제 건수={}",count));
  }

  
}
