package today.wishwordrobe.infrastructure;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import today.wishwordrobe.presentation.dto.FcmTokenDocument;
import java.util.List;
import java.time.LocalDateTime;


/**
 * FCM 토큰 저장소
 * - token(PK)으로 조회/삭제
 * - userId로 해당 사용자의 모든 토큰 조회/삭제 (로그아웃 시)
 */
public interface FcmTokenRepository extends ReactiveMongoRepository<FcmTokenDocument, String> {

    //전체 활성 토큰 조회 메서드
    Flux<FcmTokenDocument> findByIsActive(Boolean isActive);
    //특정 사용자의 모든 토큰 조회
    Flux<FcmTokenDocument> findByUserId(Long userId);

    //특정 사용자의 모든 토큰 삭제 (로그아웃)
    Mono<Void> deleteByUserId(Long userId);

    //사용자별 활성 토큰 개수 조회
    Mono<Long> countByUserIdAndIsActive(Long userId, Boolean isActive);

    //가장 오래된 토큰 조회 (디바이스 제한용)
    Mono<FcmTokenDocument> findFirstByUserIdAndIsActiveOrderByLastUsedAtAsc(Long userId, Boolean isActive);

    //특정 기간 이상 미사용 구독 조회
    Flux<FcmTokenDocument> findByLastUsedAtBeforeAndIsActive(LocalDateTime date, boolean isActive);
    Mono<FcmTokenDocument> findByToken(String token);

    Flux<FcmTokenDocument> findByUserIdAndIsActive(Long userId, Boolean isActive);
}
