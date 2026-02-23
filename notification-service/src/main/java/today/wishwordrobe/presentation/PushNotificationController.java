package today.wishwordrobe.presentation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import today.wishwordrobe.application.PushNotificationService;
import today.wishwordrobe.firebase.FCMPushNotificationRequest;
import today.wishwordrobe.presentation.dto.BroadcastJobStatus;
import today.wishwordrobe.presentation.dto.FcmTokenRequest;
import today.wishwordrobe.presentation.dto.PushNotificationRequest;
import today.wishwordrobe.webpush.BroadcastJobService;
import today.wishwordrobe.webpush.BroadcastJobService.JobView;

@Slf4j
@RestController
@RequestMapping("/notification")
public class PushNotificationController {

    @Autowired
    private BroadcastJobService broadcastJobService;

    @Autowired
    private PushNotificationService pushNotificationService;

    @Value("${webpush.public-key}")
    private String publicKey;

    public PushNotificationController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping("/public-key")
    public Mono<String> getPublicKey() {
        log.info("--------------------");
        return Mono.just(publicKey);
    }

    // 웹푸시 구독 등
    @PostMapping("/subscribe")
    public Mono<ResponseEntity<Void>> subscribe(@RequestBody Map<String, Object> subscription) {
        return pushNotificationService.saveSubscription(subscription)
                .then(Mono.just(ResponseEntity.ok().build()));
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
                        ResponseEntity.internalServerError().body("Error: " + e.getMessage())));
    }

    @PostMapping("/send-token")
    public Mono<ResponseEntity<String>> sendTokenNotification(@RequestBody FCMPushNotificationRequest request) {
        log.info("Received send-token request: token={}, title={}, message={}",
                request.getToken(), request.getTitle(), request.getMessage());

        if (request.getToken() == null) {
            log.error("Token is null in the request");
            return Mono.just(ResponseEntity.badRequest().body("Token is required"));
        }

        return pushNotificationService.sendNotification(request)
                .map(result -> {
                    log.info("Notification sent successfully: {}", result);
                    return ResponseEntity.ok("Notification sent to device: " + request.getToken());
                })
                .onErrorResume(e -> {
                    log.error("Error sending notification", e);
                    return Mono.just(
                            ResponseEntity.internalServerError().body("Error: " + e.getMessage()));
                });
    }

    // enqueue()가 jobId를 반환하는데 컨트롤러는 ResponseEntity를 반환해야하니까 map으로 감싼다.
    @PostMapping("/broadcast")
    public Mono<ResponseEntity<Map<String, Object>>> broadcast(@RequestBody PushNotificationRequest request) {
        return broadcastJobService.enqueue(request)
                .map(jobId -> ResponseEntity.accepted().<Map<String, Object>>body(Map.of("jobId", jobId)))
                .onErrorResume(e -> {
                    // // GeneralSecurityException, IOException, JoseException 전부 여기서 잡힘
                    log.error("[Broadcast] 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError()
                            .<Map<String, Object>>body(Map.of("error", e.getMessage())));
                });
    }

    // 상태 조회
    @GetMapping("/jobs/{jobId}")
    public Mono<ResponseEntity<JobView>> job(@PathVariable("jobId") String jobId) {
        return broadcastJobService.get(jobId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    // FCM 토큰 등록 (중복 방지)
    @PostMapping("/fcm/register")
    public Mono<ResponseEntity<String>> registerFcmToken(@RequestBody FcmTokenRequest request) {
        return pushNotificationService.registerFcmToken(request)
                .map(saved -> ResponseEntity.ok("FCM token registered for userId: " + saved.getUserId()))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().body("Error: " + e.getMessage())));
    }

    // 특정 FCM 토큰 삭제 (디바이스 단위 로그아웃)
    @PostMapping("/fcm/unregister")
    public Mono<ResponseEntity<String>> unregisterFcmToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null) {
            return Mono.just(ResponseEntity.badRequest().body("token is required"));
        }
        return pushNotificationService.unregisterFcmToken(token)
                .then(Mono.just(ResponseEntity.ok("FCM token unregistered")));
    }

    // 특정 사용자의 모든 FCM 토큰 삭제 (전체 로그아웃)
    @PostMapping("/fcm/unregister-user")
    public Mono<ResponseEntity<String>> unregisterAllFcmTokens(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        if (userId == null) {
            return Mono.just(ResponseEntity.badRequest().body("userId is required"));
        }
        return pushNotificationService.unregisterAllFcmTokensByUserId(userId)
                .then(Mono.just(ResponseEntity.ok("All FCM tokens unregistered for userId: " + userId)));
    }
}
