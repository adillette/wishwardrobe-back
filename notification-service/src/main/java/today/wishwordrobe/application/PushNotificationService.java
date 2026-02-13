package today.wishwordrobe.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import today.wishwordrobe.firebase.FCMPushNotificationRequest;
import today.wishwordrobe.firebase.FCMService;
import today.wishwordrobe.infrastructure.FcmTokenRepository;
import today.wishwordrobe.infrastructure.WebPushSubscriptionRepository;
import today.wishwordrobe.presentation.dto.FcmTokenDocument;
import today.wishwordrobe.presentation.dto.FcmTokenRequest;
import today.wishwordrobe.presentation.dto.BroadcastJobStatus;
import today.wishwordrobe.presentation.dto.PushNotificationRequest;
import today.wishwordrobe.presentation.dto.WebPushSubscriptionDocument;
import today.wishwordrobe.webpush.WebPushNotificationRequest;
import today.wishwordrobe.webpush.WebPushService;
import today.wishwordrobe.webpush.WebPushSendException;
import today.wishwordrobe.webpush.WebPushSubscription;

@Service
@Slf4j
public class PushNotificationService {

    @Autowired
    private WebPushSubscriptionRepository webPushSubscriptionRepository;

    @Autowired
    private FcmTokenRepository fcmTokenRepository;

    @Autowired
    private FCMService fcmService;
    @Autowired
    private WebPushService webPushService;

    private final ConcurrentHashMap<String, BroadcastJobStatus> broadcastJobs = new ConcurrentHashMap<>();

    public PushNotificationService(FCMService fcmService, WebPushService webPushService,
            WebPushSubscriptionRepository webPushSubscriptionRepository,
            FcmTokenRepository fcmTokenRepository) {
        this.fcmService = fcmService;
        this.webPushService = webPushService;
        this.webPushSubscriptionRepository = webPushSubscriptionRepository;
        this.fcmTokenRepository = fcmTokenRepository;
    }

    public record BroadcastStats(long total, long success, long failed, long gone410) {
    }

    public Mono<BroadcastStats> broadcastWebPushFromDbWithStats(PushNotificationRequest request) {
        int concurrency = 50;
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong failedCount = new AtomicLong(0);
        AtomicLong gone410Count = new AtomicLong(0);
        return webPushSubscriptionRepository.count()
                .flatMap(totalSubscribers -> webPushSubscriptionRepository.findAll()
                        .flatMap(doc -> {
                            WebPushSubscription subscription = new WebPushSubscription();
                            subscription.setEndpoint(doc.getEndpoint());

                            WebPushSubscription.Keys keys = new WebPushSubscription.Keys();
                            keys.setP256dh(doc.getP256dh());
                            keys.setAuth(doc.getAuth());
                            subscription.setKeys(keys);

                            WebPushNotificationRequest webRequest = WebPushNotificationRequest.builder()
                                    .title(request.getTitle())
                                    .message(request.getMessage())
                                    .icon(request.getIcon())
                                    .clickAction(request.getClickAction())
                                    .data(request.getData())
                                    .url(request.getUrl())
                                    .image(request.getImage())
                                    .subscription(subscription)
                                    .build();
                            return webPushService.sendNotification(webRequest)
                                    .doOnSuccess(v -> successCount.incrementAndGet()) // 성공 시 +1
                                    .onErrorResume(error -> {
                                        failedCount.incrementAndGet(); // 실패 시 +1

                                        // 410 Gone이면 구독 삭제
                                        if (error instanceof WebPushSendException ex && ex.getStatusCode() == 410) {
                                            gone410Count.incrementAndGet(); // 410 카운트 +1
                                            return webPushSubscriptionRepository
                                                    .deleteById(subscription.getEndpoint())
                                                    .then(); // 삭제 후 완료
                                        }
                                        return Mono.empty(); // 다른 에러는 무시하고 진행
                                    });
                        }, concurrency) // 50개씩 병렬 처리
                        .then(Mono.fromSupplier(() ->
                        // 3. 최종 통계 반환
                        new BroadcastStats(
                                totalSubscribers, // 전체 구독자 수
                                successCount.get(), // 성공 건수
                                failedCount.get(), // 실패 건수
                                gone410Count.get() // 410 Gone 건수
                        ))));
    }

    /**
     * FCM 토큰 등록 (중복 방지: token이 PK)
     * - 같은 token이 들어오면 기존 레코드를 덮어씀 (upsert)
     */
    public Mono<FcmTokenDocument> registerFcmToken(FcmTokenRequest request) {
        if (request.getUserId() == null || request.getToken() == null) {
            return Mono.error(new IllegalArgumentException("userId and token are required"));
        }

        FcmTokenDocument doc = FcmTokenDocument.builder()
                .token(request.getToken())
                .userId(request.getUserId())
                .deviceId(request.getDeviceId())
                .createdAt(LocalDateTime.now())
                .lastUsedAt(LocalDateTime.now())
                .build();

        return fcmTokenRepository.save(doc)
                .doOnSuccess(saved -> log.info("FCM token registered: userId={}, token={}",
                        request.getUserId(), request.getToken()));
    }

    /**
     * 특정 토큰 삭제 (디바이스 단위 로그아웃)
     */
    public Mono<Void> unregisterFcmToken(String token) {
        return fcmTokenRepository.deleteById(token)
                .doOnSuccess(v -> log.info("FCM token deleted: token={}", token));
    }

    /**
     * 특정 사용자의 모든 토큰 삭제 (전체 로그아웃)
     */
    public Mono<Void> unregisterAllFcmTokensByUserId(String userId) {
        return fcmTokenRepository.deleteByUserId(userId)
                .doOnSuccess(v -> log.info("All FCM tokens deleted for userId={}", userId));
    }

    public Mono<Void> saveSubscription(Map<String, Object> rawSubscription) {
        WebPushSubscription subscription = convertSubScription(rawSubscription);

        WebPushSubscriptionDocument doc = new WebPushSubscriptionDocument();
        doc.setEndpoint(subscription.getEndpoint());
        doc.setP256dh(subscription.getKeys().getP256dh());
        doc.setAuth(subscription.getKeys().getAuth());

        return webPushSubscriptionRepository.save(doc).then();
    }

    public Mono<Map<String, Object>> sendNotification(PushNotificationRequest request) {
        // 브로드캐스트 요청: WebPushNotificationRequest/FCMPushNotificationRequest가 아니면
        // MongoDB에 저장된 구독자 전체에게 발송한다.
        if (!(request instanceof WebPushNotificationRequest) && !(request instanceof FCMPushNotificationRequest)) {
            return broadcastWebPushFromDbWithStats(request)  // ← 메서드 이름 변경
                .map(stats -> {
                    Map<String, Object> results = new HashMap<>();
                    results.put("total", stats.total());
                    results.put("success", stats.success());
                    results.put("failed", stats.failed());
                    results.put("gone410", stats.gone410());
                    return results;
                });
        }

        // 결과를 저장할 맵
        Map<String, Object> results = new HashMap<>();
        List<Mono<?>> tasks = new ArrayList<>();

        // FCM 요청 처리 (FCMPushNotificationRequest 타입 확인)
        if (request instanceof FCMPushNotificationRequest) {
            FCMPushNotificationRequest fcmRequest = (FCMPushNotificationRequest) request;

            // 토큰 확인
            if (fcmRequest.getToken() != null) {
                tasks.add(fcmService.sendTokenMessage(fcmRequest)
                        .doOnSuccess(messageId -> {
                            results.put("fcmResult", "success");
                            results.put("fcmMessageId", messageId);
                        })
                        .onErrorResume(e -> {
                            results.put("fcmError", e.getMessage());
                            return Mono.empty();
                        }));
            }
            // 토픽 확인
            if (fcmRequest.getTopic() != null) {
                tasks.add(fcmService.sendTopicMessage(fcmRequest)
                        .doOnSuccess(messageId -> {
                            results.put("topicResult", "success");
                            results.put("topicMessageId", messageId);
                        })
                        .onErrorResume(e -> {
                            results.put("topicError", e.getMessage());
                            return Mono.empty();
                        }));
            }
        }

        // 웹푸시 요청 처리 (WebPushNotificationRequest 타입 확인)
        if (request instanceof WebPushNotificationRequest) {
            WebPushNotificationRequest webRequest = (WebPushNotificationRequest) request;

            if (webRequest.getSubscription() != null) {
                tasks.add(webPushService.sendNotification(webRequest)
                        .doOnSuccess(v -> results.put("webPushResult", "success"))
                        .onErrorResume(e -> {
                            results.put("webPushError", e.getMessage());
                            return Mono.empty();
                        }));
            }
        }

        // 모든 작업 실행 후 결과 반환
        return Mono.when(tasks).thenReturn(results);
    }

    public Mono<BroadcastJobStatus> startBroadcastJob(PushNotificationRequest request) {
        String jobId = UUID.randomUUID().toString();
        BroadcastJobStatus job = BroadcastJobStatus.pending(jobId, LocalDateTime.now());
        broadcastJobs.put(jobId, job);

        broadcastWebPushFromDbWithStats(request)
                .doOnSubscribe(sub -> job.markStarted(LocalDateTime.now()))
                .doOnSuccess(stats -> {  // stats는 BroadcastStats
                // BroadcastJobStatus의 markDone을 stats를 받도록 수정해야 함
                job.markDone(LocalDateTime.now(), stats);
            })
                .doOnError(e -> job.markFailed(LocalDateTime.now(), e))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(job);
    }

    public Mono<BroadcastJobStatus> getBroadcastJob(String jobId) {
        BroadcastJobStatus job = broadcastJobs.get(jobId);
        if (job == null) {
            return Mono.empty();
        }
        return Mono.just(job);
    }

    private Mono<Map<String, Object>> broadcastWebPushFromDb(PushNotificationRequest request) {
        return webPushSubscriptionRepository.count()
                .flatMap(count -> {
                    Map<String, Object> results = new HashMap<>();
                    results.put("broadcastCount", count);

                    return webPushSubscriptionRepository.findAll()
                            .flatMap(doc -> {
                                WebPushSubscription subscription = new WebPushSubscription();
                                subscription.setEndpoint(doc.getEndpoint());

                                WebPushSubscription.Keys keys = new WebPushSubscription.Keys();
                                keys.setP256dh(doc.getP256dh());
                                keys.setAuth(doc.getAuth());
                                subscription.setKeys(keys);

                                WebPushNotificationRequest webRequest = WebPushNotificationRequest.builder()
                                        .title(request.getTitle())
                                        .message(request.getMessage())
                                        .icon(request.getIcon())
                                        .clickAction(request.getClickAction())
                                        .data(request.getData())
                                        .url(request.getUrl())
                                        .subscription(subscription)
                                        .build();

                                return webPushService.sendNotification(webRequest)
                                        .doOnError(e -> log.error("WebPush send failed: endpoint={}", doc.getEndpoint(),
                                                e))
                                        .onErrorResume(e -> {
                                            if (e instanceof WebPushSendException ex && ex.getStatusCode() == 410) {
                                                log.warn(
                                                        "WebPush subscription expired (410 Gone). Re-subscribe required. endpoint={}",
                                                        doc.getEndpoint());
                                                // 만료된 구독은 DB에서 제거 (다음 broadcast에서 재시도 안 함)
                                                return webPushSubscriptionRepository.deleteById(doc.getEndpoint())
                                                        .then(Mono.empty());
                                            }
                                            return Mono.empty();
                                        });
                            })
                            .then(Mono.just(results));
                });
    }

    private WebPushSubscription convertSubScription(Map<String, Object> rawSubscription) {
        WebPushSubscription subscription = new WebPushSubscription();
        subscription.setEndpoint((String) rawSubscription.get("endpoint"));

        Map<String, String> keysMap = (Map<String, String>) rawSubscription.get("keys");
        WebPushSubscription.Keys keys = new WebPushSubscription.Keys();
        keys.setP256dh(keysMap.get("p256dh"));
        keys.setAuth(keysMap.get("auth"));

        subscription.setKeys(keys);
        return subscription;

    }

}
