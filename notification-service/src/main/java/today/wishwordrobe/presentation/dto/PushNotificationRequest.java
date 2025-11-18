package today.wishwordrobe.presentation.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@SuperBuilder
public class PushNotificationRequest {
    private String title;
    private String message;
//    private String topic;
//    private String token;
    private String icon;
    private String clickAction;
    private Map<String, String> data;
    private String url; //알림 클릭하면 이동할 url




    public PushNotificationRequest(String title, String message, String icon, String clickAction, Map<String, String> data, String url) {
        this.title = title;
        this.message = message;
        this.icon = icon;
        this.clickAction = clickAction;
        this.data = data;
        this.url = url;
    }
}
