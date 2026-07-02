package today.wishwordrobe.infrastructure;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface UserLocationRepository extends ReactiveMongoRepository<UserLocationDocument,String>{
  //userId로 단건 조회
  Mono<UserLocationDocument> findByUserId(long userId);
  //알림 활성화된 전체 사용자 조회 스케줄러가 호출
  Flux<UserLocationDocument> findByEnabled(boolean enabled);
  // 사용자 위치 삭제 (탈퇴 시)
    // Mono<Void> deleteByUserId(long userId);

}
