package today.wishwordrobe.presentation;

import today.wishwordrobe.application.PushNotificationService;
import today.wishwordrobe.firebase.FCMPushNotificationRequest;
import today.wishwordrobe.presentation.dto.PushNotificationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
@RequestMapping("/api/notification")
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;


    public PushNotificationController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping("/public-key")
    public Mono<String> getPublicKey(){
        return Mono.just("단순한 공개키 제공");//이거 initializer에다가 설정했는데 어떻게 불러오지
    }

    //구독 정보 저장
    //private List<Map<String, Object>> subscriptions = new ArrayList<>();

    @PostMapping("/subscribe")
    public Mono<ResponseEntity<Void>> subscribe(@RequestBody Map<String, Object> subscription) {
        return pushNotificationService.saveSubscription(subscription)
                .then(Mono.just(ResponseEntity.ok().build()));
    }


    @PostMapping("/send")
    public Mono<ResponseEntity<String>> sendNotification(@RequestBody PushNotificationRequest request) {
        return pushNotificationService.sendNotification(request)
                .map(result -> ResponseEntity.ok("Notification sent successfully"))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.internalServerError().body(
                                "Error sending notification: "
                                        + e.getMessage())
                ));
    }
/*
* 여기 부터 dto가 바뀐거 때문에 변경해야한다.
 */
    @PostMapping("/send-topic")
    public Mono<ResponseEntity<String>> sendTopicNotification(@RequestBody FCMPushNotificationRequest request) {
        if (request.getTopic() == null) {
            return Mono.just(ResponseEntity.badRequest().body("Topic is required"));
        }

        return pushNotificationService.sendNotification(request)
                .map(result -> ResponseEntity.ok("Notification sent to topic: " + request.getTopic()))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.internalServerError().body("Error: " + e.getMessage())
                ));
    }

    @PostMapping("/send-token")
    public Mono<ResponseEntity<String>> sendTokenNotification(@RequestBody FCMPushNotificationRequest request) {
        if (request.getToken() == null) {
            return Mono.just(ResponseEntity.badRequest().body("Token is required"));
        }

        return pushNotificationService.sendNotification(request)
                .map(result -> ResponseEntity.ok("Notification sent to device: " + request.getToken()))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.internalServerError().body("Error: " + e.getMessage())
                ));
    }

    @PostMapping("/broadcast")
    public Mono<ResponseEntity<Map<String, Object>>> broadcast(@RequestBody PushNotificationRequest request) {
        return pushNotificationService.sendNotification(request)
                .map(result -> ResponseEntity.ok(result))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.internalServerError().body(
                                Map.of("error", e.getMessage())
                        )
                ));
    }
}





    /*
   //테스트용
       @PostMapping("/subscribe")
       public ResponseEntity<String> subscribeToNotifications(@RequestBody String subscription) {
           // 구독 정보를 저장하는 로직
           subscriptionService.saveSubscription(subscription);
           return ResponseEntity.ok("Subscription saved successfully!");
       }

   //테스트용
       @GetMapping("/send-notification")
       public ResponseEntity<String> sendNotification() {
           // 저장된 구독 정보를 사용하여 알림 전송
           pushNotificationService.sendPushNotificationToAllSubscribers(
                   "테스트 알림 제목",
                   "이것은 테스트 알림 내용입니다."
           );
           return ResponseEntity.ok("Notification sent!");
       }

   */



