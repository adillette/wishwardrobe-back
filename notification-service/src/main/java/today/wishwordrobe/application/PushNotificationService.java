package today.wishwordrobe.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import today.wishwordrobe.firebase.FCMPushNotificationRequest;
import today.wishwordrobe.firebase.FCMService;
import today.wishwordrobe.infrastructure.WebPushSubscriptionRepository;
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
    private FCMService fcmService;
    @Autowired
    private WebPushService webPushService;

    public PushNotificationService(FCMService fcmService, WebPushService webPushService,
            WebPushSubscriptionRepository webPushSubscriptionRepository) {
        this.fcmService = fcmService;
        this.webPushService = webPushService;
        this.webPushSubscriptionRepository = webPushSubscriptionRepository;
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
            return broadcastWebPushFromDb(request);
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
                                                log.warn("WebPush subscription expired (410 Gone). Re-subscribe required. endpoint={}", doc.getEndpoint());
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
