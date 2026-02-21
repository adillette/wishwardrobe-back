package today.wishwordrobe.presentation.dto;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

//무슨 알림을 보낼지 
//알림 발송에 필요한 공통 필드를 한곳에 모아두고 같은 메시지 구조재사용 가능
@Data
@NoArgsConstructor
@SuperBuilder
public class PushNotificationRequest {
    private String title;
    private String message;
    private String icon;
    private String clickAction;
    private Map<String, String> data;
    private String url; // 알림 클릭하면 이동할 url

    private String image;

    public PushNotificationRequest(String title, String message,
            String icon, String clickAction,
            Map<String, String> data, String url,
            String image) {
        this.title = title;
        this.message = message;
        this.icon = icon;
        this.clickAction = clickAction;
        this.data = data;
        this.url = url;
        this.image = image;

    }
}
