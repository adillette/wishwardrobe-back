package today.wishwordrobe.webpush;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebPushSubscription {
    private String endpoint;

    private Keys keys;

    @Data
    public static class Keys{
        private String p256dh;
        private String auth;
    }
}
