package today.wishwordrobe.infrastructure;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import today.wishwordrobe.presentation.dto.WebPushSubscriptionDocument;

public interface WebPushSubscriptionRepository extends ReactiveMongoRepository<WebPushSubscriptionDocument,String>{
  //사용자별 활성 구독 개수
  Mono<Long> countByUserIdAndIsActive(String userId, boolean isActive);
  Flux<WebPushSubscriptionDocument> findByIsActive(boolean isActive);
 //가장 오래된 구독
  Mono<WebPushSubscriptionDocument> findFirstByUserIdAndIsActiveOrderByLastUsedAtAsc(String userId, boolean isActive);
  
  Flux<WebPushSubscriptionDocument> findByUserIdAndIsActiveOrderByLastUsedAtAsc(String userId, boolean isActive);
  
  //사용자별 활성 구독 목록
  Flux<WebPushSubscriptionDocument> findByUserIdAndIsActive(String userId,boolean isActive);

  //특정 기간 이상 미사용 구독 조회
  Flux<WebPushSubscriptionDocument> findByLastUsedAtBeforeAndIsActive(LocalDateTime date, boolean isActive);
//사용자 전체 구독 삭제
  Mono<Void> deleteByUserId(String userId);
}
