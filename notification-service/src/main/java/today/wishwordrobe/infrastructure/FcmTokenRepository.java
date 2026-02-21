package today.wishwordrobe.infrastructure;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import today.wishwordrobe.presentation.dto.FcmTokenDocument;

/**
 * FCM 토큰 저장소
 * - token(PK)으로 조회/삭제
 * - userId로 해당 사용자의 모든 토큰 조회/삭제 (로그아웃 시)
 */
public interface FcmTokenRepository extends ReactiveMongoRepository<FcmTokenDocument, String> {

    //전체 활성 토큰 조회 메서드
    Flux<FcmTokenDocument> findByIsActive(boolean isActive);
    //특정 사용자의 모든 토큰 조회
    Flux<FcmTokenDocument> findByUserId(String userId);

    //특정 사용자의 모든 토큰 삭제 (로그아웃)
    Mono<Void> deleteByUserId(String userId);

    //사용자별 활성 토큰 개수 조회
    Mono<Long> countByUserIdAndIsActive(String userId, boolean isActive);

    //가장 오래된 토큰 조회 (디바이스 제한용)
    Mono<FcmTokenDocument> findFirstByUserIdAndIsActiveOrderByLastUsedAtAsc(String userId, boolean isActive);

    Mono<FcmTokenDocument> findByToken(String token);

    Flux<FcmTokenDocument> findByUserIdAndIsActive(String userId, boolean isActive);
}
