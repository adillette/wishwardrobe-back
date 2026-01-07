package today.wishwordrobe.presentation.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import today.wishwordrobe.webpush.WebPushSubscription;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "webpush_subscriptions")
public class WebPushSubscriptionDocument {
 
    @Id
    private String endpoint;   // endpoint를 PK처럼 사용 (중복 방지)

    private String p256dh;
    private String auth;

    public static WebPushSubscriptionDocument from(WebPushSubscription subscription) {
        if (subscription == null || subscription.getEndpoint() == null) {
            throw new IllegalArgumentException("subscription/endpoint is required");
        }
        String p256dh = subscription.getKeys() != null ? subscription.getKeys().getP256dh() : null;
        String auth = subscription.getKeys() != null ? subscription.getKeys().getAuth() : null;
        return new WebPushSubscriptionDocument(subscription.getEndpoint(), p256dh, auth);
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
