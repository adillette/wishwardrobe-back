package today.wishwordrobe.firebase;

import java.util.Map;

import lombok.Data;
import today.wishwordrobe.presentation.dto.PushNotificationRequest;

@Data
public class FCMPushNotificationRequest extends PushNotificationRequest {

    private String topic;
    private String token;

    public FCMPushNotificationRequest(String title, String message, String icon, String clickAction,
                                      Map<String, String> data, String url, String topic, String token) {
        super(title, message, icon, clickAction, data, url,topic);
        this.topic = topic;
        this.token = token;
    }
}
