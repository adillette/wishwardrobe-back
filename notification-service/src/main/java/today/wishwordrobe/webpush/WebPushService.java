package today.wishwordrobe.webpush;



import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutionException;
import java.io.IOException;
import java.security.GeneralSecurityException;


@Service
public class WebPushService {

    private final PushService pushService;

    public WebPushService(PushService pushService) {
        this.pushService = pushService;
    }

    public Mono<Void> sendNotification(WebPushNotificationRequest request) {
        return Mono.fromRunnable(() -> {
            try {
                WebPushSubscription subscription = request.getSubscription();
                String payload = createPayload(request);

                Notification notification = new Notification(
                        subscription.getEndpoint(),
                        subscription.getKeys().getP256dh(),
                        subscription.getKeys().getAuth(),
                        payload.getBytes()
                );

                pushService.send(notification);
            } catch (GeneralSecurityException | IOException | JoseException | ExecutionException | InterruptedException e) {
                throw new RuntimeException("Error sending web push notification", e);
            }
        });
    }

    private String createPayload(WebPushNotificationRequest request) {
        // JSON으로 변환하는 로직 구현
        return String.format(
                "{\"title\":\"%s\",\"message\":\"%s\",\"icon\":\"%s\",\"clickAction\":\"%s\"}",
                request.getTitle(),
                request.getMessage(),
                request.getIcon(),
                request.getClickAction()
        );
    }
}