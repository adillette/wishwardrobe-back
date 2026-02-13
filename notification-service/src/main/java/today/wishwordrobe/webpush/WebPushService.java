package today.wishwordrobe.webpush;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class WebPushService {

    @Autowired
    private PushService pushService;
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Mono<Void> sendNotification(WebPushNotificationRequest request) {
        return Mono.fromCallable(() -> {
            WebPushSubscription subscription = request.getSubscription();
            String payload = createPayload(request);

            Notification notification = new Notification(
                subscription.getEndpoint(),
                subscription.getKeys().getP256dh(),
                subscription.getKeys().getAuth(),
                payload.getBytes(StandardCharsets.UTF_8)
            );

            var response = pushService.send(notification);
            int status = response.getStatusLine().getStatusCode();
            String body = null;
            
            if (response.getEntity() != null) {
                body = org.apache.http.util.EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
            
            if (status < 200 || status >= 300) {
                log.error("Web push failed: status={}, endpoint={}, body={}", status, subscription.getEndpoint(), body);
                throw new WebPushSendException(subscription.getEndpoint(), status, body);
            }
            
            log.info("WebPush sent successfully: endpoint={}, status={}", subscription.getEndpoint(), status);
            
            return null;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then()
        .doOnError(e -> log.error("Error sending web push notification", e));
    }

    private String createPayload(WebPushNotificationRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", request.getTitle());
        payload.put("message", request.getMessage());
        payload.put("icon", request.getIcon());
        payload.put("clickAction", request.getClickAction());
        payload.put("data", request.getData());
        payload.put("url", request.getUrl());
        payload.put("image", request.getImage());

        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error creating web push payload JSON", e);
        }
    }
}