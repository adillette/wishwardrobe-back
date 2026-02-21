package today.wishwordrobe.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import today.wishwordrobe.firebase.FCMPushNotificationRequest;
import today.wishwordrobe.firebase.FCMService;
import today.wishwordrobe.infrastructure.FcmTokenRepository;
import today.wishwordrobe.infrastructure.WebPushSubscriptionRepository;
import today.wishwordrobe.presentation.dto.FcmTokenDocument;
import today.wishwordrobe.presentation.dto.FcmTokenRequest;
import today.wishwordrobe.presentation.dto.PushNotificationRequest;
import today.wishwordrobe.webpush.WebPushNotificationRequest;
import today.wishwordrobe.webpush.WebPushService;
import today.wishwordrobe.webpush.WebPushService.SendResult;
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

    @Autowired
    private MeterRegistry meterRegistry;

    private final AtomicLong inFlight = new AtomicLong(0);
      //inFlight: 지금 이순간 처리중인 브로드캐스트 수
      
    @PostConstruct
    public void initMetrics(){
        Gauge.builder("broadcast_inflight",inFlight,AtomicLong::get)
        
        .register(meterRegistry);
    }

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

    public Mono<BroadcastStats> broadcastAllFromDbWithStats(PushNotificationRequest request) {
        int concurrency = 50;
        AtomicLong wpTotal = new AtomicLong(0);
        AtomicLong wpSuccess = new AtomicLong(0);
        AtomicLong wpFailed = new AtomicLong(0);
        AtomicLong wpGone410 = new AtomicLong(0);

        AtomicLong fcmTotal = new AtomicLong();
        AtomicLong fcmSuccess = new AtomicLong();
        AtomicLong fcmFailed = new AtomicLong();

        Counter successCounter = meterRegistry.counter("broadcast_success_total");
        Counter failedCounter = meterRegistry.counter("broadcast_failed_total");
        


        // WebPush 브로드캐스트
        Mono<Void> webPushBroadcast = webPushSubscriptionRepository.findByIsActive(true)
                .flatMap(doc -> {
                    wpTotal.incrementAndGet(); // ← 실제 발송 대상만 카운트
                    WebPushNotificationRequest webRequest = WebPushNotificationRequest.builder()
                            .title(request.getTitle())
                            .message(request.getMessage())
                            .icon(request.getIcon())
                            .clickAction(request.getClickAction())
                            .data(request.getData())
                            .url(request.getUrl())
                            .image(request.getImage())
                            .build();
                    return webPushService.sendNotification(doc, webRequest)
                            .doOnSuccess(result -> {
                                successCounter.increment();
                                if (result == SendResult.SUCCESS)
                                    wpSuccess.incrementAndGet();
                                if (result == SendResult.EXPIRED)
                                    wpGone410.incrementAndGet();
                            })
                            .onErrorResume(e -> {
                                failedCounter.increment();
                                wpFailed.incrementAndGet();
                                return Mono.<SendResult>empty(); // 타입 명시
                            });
                }, concurrency)
                .then();

        // FCM 브로드캐스트
        Mono<Void> fcmBroadcast = fcmTokenRepository.findByIsActive(true)
                .flatMap(tokenDoc -> {
                    fcmTotal.incrementAndGet(); // ← count() 제거, 실제 발송 대상만 카운트
                    FCMPushNotificationRequest fcmRequest = FCMPushNotificationRequest.builder()
                            .token(tokenDoc.getToken())
                            .title(request.getTitle())
                            .message(request.getMessage())
                            .build();
                    return fcmService.sendTokenMessage(fcmRequest)
                            .doOnSuccess(id -> { // ← 중괄호 추가
                                successCounter.increment();
                                fcmSuccess.incrementAndGet();
                            })
                            .onErrorResume(e -> {
                                failedCounter.increment();
                                fcmFailed.incrementAndGet();
                                return Mono.<String>empty();
                            });
                }, concurrency)
                .then();

        // 둘 다 병렬 실행 후 통합 통계 반환
        return Mono.when(webPushBroadcast, fcmBroadcast).thenReturn(new BroadcastStats(wpTotal.get() + fcmTotal.get(),
                wpSuccess.get() + fcmSuccess.get(), wpFailed.get() + fcmFailed.get(), wpGone410.get()));
    }

    public Mono<Void> saveSubscription(Map<String, Object> subscriptionMap) {
        String userId = (String) subscriptionMap.get("userId");

        WebPushSubscription subscription = new WebPushSubscription();

        subscription.setEndpoint((String) subscriptionMap.get("endpoint"));

        Map<String, String> keys = (Map<String, String>) subscriptionMap.get("keys");

        WebPushSubscription.Keys k = new WebPushSubscription.Keys();

        k.setP256dh(keys.get("p256dh"));

        k.setAuth(keys.get("auth"));

        subscription.setKeys(k);

        return webPushService.saveWebPushSubscription(userId, subscription).then();
    }

    // FCMService에 위임
    public Mono<FcmTokenDocument> registerFcmToken(FcmTokenRequest request) {
        return fcmService.saveToken(request);
    }

    // 토큰 1개 삭제
    public Mono<Void> unregisterFcmToken(String token) {
        return fcmTokenRepository.deleteById(token)
                .doOnSuccess(v -> log.info("FCM 토큰 삭제: token={}", token));
    }

    // 사용자 전체 토큰 삭제
    public Mono<Void> unregisterAllFcmTokensByUserId(String userId) {
        return fcmTokenRepository.deleteByUserId(userId)
                .doOnSuccess(v -> log.info("사용자 FCM 토큰 전체 삭제: userId={}", userId));
    }

    public Mono<Map<String, Object>> sendNotification(PushNotificationRequest request) {
        // 브로드캐스트 요청: WebPushNotificationRequest/FCMPushNotificationRequest가 아니면
        // MongoDB에 저장된 구독자 전체에게 발송한다.
        if (!(request instanceof FCMPushNotificationRequest)) {
            return broadcastAllFromDbWithStats(request) // ← 메서드 이름 변경
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

        // 모든 작업 실행 후 결과 반환
        return Mono.when(tasks).thenReturn(results);
    }

    public Mono<Void> unregisterWebPushSubscription(String endpoint) {
        return webPushSubscriptionRepository.deleteById(endpoint)
                .doOnSuccess(v -> log.info("WebPush 구독 해제: endpoint={}", endpoint));
    }

    public Mono<Void> unregisterAllWebPushByUserId(String userId) {
        return webPushSubscriptionRepository.deleteByUserId(userId)
                .doOnSuccess(v -> log.info("사용자 WebPush 구독 전체 삭제: userId={}", userId));
    }

}
