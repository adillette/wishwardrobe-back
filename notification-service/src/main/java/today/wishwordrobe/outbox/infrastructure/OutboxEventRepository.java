package today.wishwordrobe.outbox.infrastructure;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import today.wishwordrobe.outbox.domain.OutboxEvent;
import today.wishwordrobe.outbox.domain.OutboxStatus;

public interface OutboxEventRepository extends ReactiveMongoRepository<OutboxEvent,String>{

  // OutboxProcessor 재처리 대상 조회
  Flux<OutboxEvent> findByStatusAndRetryCountLessThan(
            OutboxStatus status, int maxRetry);
  //실패 상태 중 재시도 횟수 초과한 것 조회
  //모니터링 및 수동 재처리 대상 확인
  Flux<OutboxEvent> findByStatusAndRetryCountGreaterThanEqual(OutboxStatus status,int maxRetry);
  
  //클린업 스케줄러용
  //7일 이상 지난 sent건 삭제해서 컬렉션 비대화 방지
  Flux<OutboxEvent> findByStatusAndProcessedAtBefore(OutboxStatus status,LocalDateTime before);

  // 특정 userId의 미처리 건 조회
  //사용자별 알림 현황 확인용
  Flux<OutboxEvent> findByUserIdAndStatus(Long userId, OutboxStatus status);

} 