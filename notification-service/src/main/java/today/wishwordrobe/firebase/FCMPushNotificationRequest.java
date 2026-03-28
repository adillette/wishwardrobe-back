package today.wishwordrobe.firebase;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import today.wishwordrobe.presentation.dto.PushNotificationRequest;

@NoArgsConstructor
@Data
@SuperBuilder
public class FCMPushNotificationRequest extends PushNotificationRequest {

    private String topic;
    private String token;

    public FCMPushNotificationRequest(String title, String message, String icon, String clickAction,
                                      Map<String, String> data, String url, String topic, String token) {
        super(title, message, icon, clickAction, data, url, null);
        this.topic = topic;
        this.token = token;
    }
}
