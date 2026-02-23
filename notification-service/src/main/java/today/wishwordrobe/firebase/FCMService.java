package today.wishwordrobe.firebase;

import com.google.firebase.messaging.*;

import io.netty.handler.timeout.TimeoutException;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.transaction.annotation.Transactional;
import today.wishwordrobe.infrastructure.FcmTokenRepository;
import today.wishwordrobe.presentation.dto.FcmTokenDocument;
import today.wishwordrobe.presentation.dto.FcmTokenRequest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class FCMService {

    @Autowired
    private FcmTokenRepository fcmtokenRepository;
    private final Logger logger = LoggerFactory.getLogger(FCMService.class);

    private static final int MAX_DEVICES_PER_USER = 3;

    public Mono<String> sendPushNotification(FCMPushNotificationRequest request) {
        return Mono.fromCallable(() -> {
            Message message = prepareMessage(request);
            return sendAndGetResponse(message);
        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> sendTopicMessage(FCMPushNotificationRequest request) {
        return Mono.fromCallable(() -> {
            Message.Builder builder = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getMessage())
                            .build())
                    .setTopic(request.getTopic());

            if (request.getData() != null && !request.getData().isEmpty()) {
                builder.putAllData(request.getData());
            }

            Message message = builder.build();
            return sendAndGetResponse(message);
        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> sendTokenMessage(FCMPushNotificationRequest request) {
        return Mono.fromCallable(() -> {
            Message.Builder builder = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getMessage())
                            .build())
                    .setToken(request.getToken());

            if (request.getData() != null && !request.getData().isEmpty()) {
                builder.putAllData(request.getData());
            }

            Message message = builder.build();
            return sendAndGetResponse(message);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(response ->
                    // 전송 성공 시 lastUsedAt 업데이트
                    updateLastUsedAt(request.getToken()).thenReturn(response)
                )
                .onErrorResume(ExecutionException.class, e ->
                    handleExecutionException(request.getToken(), e)
                )
                .onErrorResume(TimeoutException.class,e->{
                    log.warn("FCM SDK 타임아웃 token={}",request.getToken());
                    return Mono.error(e);
                });
    }

    private Message prepareMessage(FCMPushNotificationRequest request) {
        Message.Builder builder = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getMessage())
                        .build());

        if (request.getTopic() != null) {
            builder.setTopic(request.getTopic());
        } else if (request.getToken() != null) {
            builder.setToken(request.getToken());
        }

        if (request.getData() != null) {
            builder.putAllData(request.getData());
        }
        // collapseKey로 사용할 값을 결정
        String collapseKey = request.getTopic() != null ? request.getTopic()
                : (request.getToken() != null ? "token-msg" : "default-key");
        AndroidConfig androidConfig = getAndroidConfig(collapseKey, request);
        return builder
                .setAndroidConfig(androidConfig)
                .build();
    }

    private AndroidConfig getAndroidConfig(String collapseKey, FCMPushNotificationRequest request) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofHours(2).toMillis())
                .setCollapseKey(collapseKey)
                .setNotification(AndroidNotification.builder()
                        .setIcon(request.getIcon())
                        .setClickAction(request.getClickAction())
                        .setSound("default")
                        .build())
                .setPriority(AndroidConfig.Priority.HIGH)
                .build();
    }

    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException,java.util.concurrent.TimeoutException {
        return FirebaseMessaging.getInstance()
        .sendAsync(message)
        .get(5,TimeUnit.SECONDS);
    }

    // ================================================================
    // 구독 Lifecycle 관리
    // ================================================================

    /**
     * 1. FCM 토큰 저장 (MongoDB)
     */
    @Transactional
    public Mono<FcmTokenDocument> saveToken(FcmTokenRequest request) {
        return fcmtokenRepository.findByToken(request.getToken())
                .flatMap(existing -> {
                    existing.setLastUsedAt(LocalDateTime.now());
                    existing.setIsActive(true);
                    log.info("기존 FCM 토큰 재활성화 - userId: {}", request.getUserId());
                    return fcmtokenRepository.save(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    FcmTokenDocument token = FcmTokenDocument.builder()
                            .token(request.getToken())
                            .userId(request.getUserId())
                            .deviceId(request.getDeviceId())
                            .createdAt(LocalDateTime.now())
                            .lastUsedAt(LocalDateTime.now())
                            .isActive(true)
                            .build();
                    log.info("새 FCM 토큰 생성 - userId: {}", request.getUserId());
                    return fcmtokenRepository.save(token);
                }))
                .flatMap(saved ->
                    // 4. 디바이스 개수 제한 체크 (최대 3개)
                    enforceDeviceLimit(request.getUserId()).thenReturn(saved)
                )
                .doOnSuccess(saved ->
                    log.info("FCM 토큰 저장 완료 - userId: {}, token: {}", saved.getUserId(), saved.getToken())
                );
    }

    /**
     * 4. 사용자당 최대 3개 디바이스 제한
     */
    private Mono<Void> enforceDeviceLimit(String userId) {
        return fcmtokenRepository.countByUserIdAndIsActive(userId, true)
                .flatMap(count -> deleteOldestIfExceedsLimit(userId, count));
    }

    private Mono<Void> deleteOldestIfExceedsLimit(String userId, long count) {
        if (count <= MAX_DEVICES_PER_USER) {
            return Mono.empty();
        }
        return fcmtokenRepository.findFirstByUserIdAndIsActiveOrderByLastUsedAtAsc(userId, true)
                .flatMap(oldest -> {
                    log.warn("디바이스 제한 초과 - 가장 오래된 디바이스 삭제: userId={}, deviceId={}, lastUsedAt={}",
                            userId, oldest.getDeviceId(), oldest.getLastUsedAt());
                    return fcmtokenRepository.delete(oldest)
                            .then(deleteOldestIfExceedsLimit(userId, count - 1));
                });
    }

    /**
     * 사용자별로 메시지 전송
     */
    public Flux<String> sendMessageToUser(String userId, String title, String body) {
        return fcmtokenRepository.findByUserIdAndIsActive(userId, true)
                .doOnSubscribe(s -> log.info("사용자 FCM 메시지 전송 시작 - userId: {}", userId))
                .flatMap(fcmToken -> {
                    FCMPushNotificationRequest req = FCMPushNotificationRequest.builder()
                            .token(fcmToken.getToken())
                            .title(title)
                            .message(body)
                            .build();
                    return sendTokenMessage(req)
                            .onErrorResume(e -> {
                                log.error("메시지 전송 실패 - token: {}", fcmToken.getToken(), e);
                                return Mono.empty();
                            });
                });
    }

    /**
     * 2. UNREGISTERED = HTTP 410 Gone (토큰 만료) → 자동 삭제
     */
    private Mono<String> handleExecutionException(String token, ExecutionException e) {
        if (e.getCause() instanceof FirebaseMessagingException exception) {
            String errorCode = exception.getMessagingErrorCode().name();
            log.error("FCM 메시지 전송 실패 - errorCode: {}, token: {}", errorCode, token);

            if ("UNREGISTERED".equals(errorCode)) {
                return deleteExpiredToken(token).then(Mono.empty());
            }
        } else {
            log.error("FCM 메시지 전송 실행 오류 - token: {}", token, e);
        }
        return Mono.empty();
    }

    /**
     * 토큰 마지막 사용 시간 업데이트
     */
    @Transactional
    public Mono<Void> updateLastUsedAt(String token) {
        return fcmtokenRepository.findByToken(token)
                .flatMap(fcmToken -> {
                    fcmToken.setLastUsedAt(LocalDateTime.now());
                    return fcmtokenRepository.save(fcmToken);
                })
                .doOnSuccess(t -> log.debug("FCM 토큰 마지막 사용 시간 업데이트 - token: {}", token))
                .then();
    }

    /**
     * 만료된 토큰 삭제
     */
    @Transactional
    public Mono<Void> deleteExpiredToken(String token) {
        return fcmtokenRepository.findByToken(token)
                .flatMap(fcmToken -> {
                    log.warn("만료된 FCM 토큰 삭제 - userId: {}, deviceId: {}, token: {}",
                            fcmToken.getUserId(), fcmToken.getDeviceId(), token);
                    return fcmtokenRepository.delete(fcmToken);
                });
    }

}
