package today.wishwordrobe.application;


import today.wishwordrobe.webpush.WebPushNotificationRequest;
import today.wishwordrobe.webpush.WebPushService;
import today.wishwordrobe.webpush.WebPushSubscription;
import today.wishwordrobe.firebase.FCMPushNotificationRequest;
import today.wishwordrobe.firebase.FCMService;
import today.wishwordrobe.presentation.dto.PushNotificationRequest;
import org.checkerframework.checker.units.qual.Current;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PushNotificationService {

    private final FCMService fcmService;
    private final WebPushService webPushService;


    //브라우저 구독 정보를 저장하는 hash맵
    private final Map<String, WebPushSubscription> subscriptions=
            new ConcurrentHashMap<>();

    public PushNotificationService(FCMService fcmService, WebPushService webPushService) {
        this.fcmService = fcmService;
        this.webPushService = webPushService;
    }

    public Mono<Void> saveSubscription(Map<String, Object> rawSubscription){
        return Mono.fromRunnable(()->{
            WebPushSubscription subscription = convertSubScription(rawSubscription);
            subscriptions.put(subscription.getEndpoint(),subscription);
        });
    }

    public Mono<Map<String,Object>> sendNotification(PushNotificationRequest request){
        //결과를 저장할 맵
        Map<String, Object> results= new HashMap<>();
        List<Mono<?>> tasks= new ArrayList<>();

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

        // 브로드캐스트 처리
        // (여기서는 기존 구독자 목록을 활용)
        if (!(request instanceof WebPushNotificationRequest) && !(request instanceof FCMPushNotificationRequest)) {
            results.put("broadcastCount", subscriptions.size());

            for (WebPushSubscription subscription : subscriptions.values()) {
                // 새로운 웹푸시 요청 객체 생성
                WebPushNotificationRequest webRequest = WebPushNotificationRequest.builder()
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .icon(request.getIcon())
                        .clickAction(request.getClickAction())
                        .data(request.getData())
                        .url(request.getUrl())
                        .subscription(subscription)
                        .build();

                tasks.add(webPushService.sendNotification(webRequest)
                        .onErrorResume(e -> Mono.empty()));
            }
        }

        // 모든 작업 실행 후 결과 반환
        return Mono.when(tasks).thenReturn(results);
    }

    private WebPushSubscription convertSubScription(Map<String, Object> rawSubscription) {
        WebPushSubscription subscription = new WebPushSubscription();
        subscription.setEndpoint((String) rawSubscription.get("endpoint"));

        Map<String, String> keysMap = (Map<String,String>)rawSubscription.get("keys");
        WebPushSubscription.Keys keys= new WebPushSubscription.Keys();
        keys.setP256dh(keysMap.get("p256dh"));
        keys.setAuth(keysMap.get("auth"));

        subscription.setKeys(keys);
        return subscription;

    }

}
