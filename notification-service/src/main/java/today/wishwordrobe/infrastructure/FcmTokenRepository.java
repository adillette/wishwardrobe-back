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

    /**
     * 특정 사용자의 모든 토큰 조회
     */
    Flux<FcmTokenDocument> findByUserId(String userId);

    /**
     * 특정 사용자의 모든 토큰 삭제 (로그아웃)
     */
    Mono<Void> deleteByUserId(String userId);
}
