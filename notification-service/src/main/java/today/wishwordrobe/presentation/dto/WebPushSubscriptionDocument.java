package today.wishwordrobe.presentation.dto;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import today.wishwordrobe.webpush.WebPushSubscription;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "webpush_subscriptions")
public class WebPushSubscriptionDocument {

    @Id
    private String endpoint;   // endpoint를 PK처럼 사용 (중복 방지)

    private String p256dh;
    private String auth;

    @Indexed
    private String userId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastUsedAt;

    @Builder.Default
    private boolean isActive = true;

    public static WebPushSubscriptionDocument from(WebPushSubscription subscription, String userId) {
        if (subscription == null || subscription.getEndpoint() == null) {
            throw new IllegalArgumentException("subscription/endpoint is required");
        }
        String p256dh = subscription.getKeys() != null ? subscription.getKeys().getP256dh() : null;
        String auth = subscription.getKeys() != null ? subscription.getKeys().getAuth() : null;
        return WebPushSubscriptionDocument.builder()
                .endpoint(subscription.getEndpoint())
                .p256dh(p256dh)
                .auth(auth)
                .userId(userId)
                .lastUsedAt(LocalDateTime.now())
                .build();
    }

    public WebPushSubscription toDomain() {
        WebPushSubscription sub = new WebPushSubscription();
        sub.setEndpoint(this.endpoint);

        WebPushSubscription.Keys keys = new WebPushSubscription.Keys();
        keys.setP256dh(this.p256dh);
        keys.setAuth(this.auth);

        sub.setKeys(keys);
        return sub;
    }

}
