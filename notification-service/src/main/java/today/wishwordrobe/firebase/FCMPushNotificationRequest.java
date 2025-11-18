package today.wishwordrobe.firebase;

import today.wishwordrobe.presentation.dto.PushNotificationRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@Setter
@SuperBuilder
public class FCMPushNotificationRequest extends PushNotificationRequest {

    private String topic;
    private String token;

    public FCMPushNotificationRequest(String title, String message, String icon, String clickAction,
                                      Map<String, String> data, String url, String topic, String token) {
        super(title, message, icon, clickAction, data, url);
        this.topic = topic;
        this.token = token;
    }
}
