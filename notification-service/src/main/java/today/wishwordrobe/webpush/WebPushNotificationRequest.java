package today.wishwordrobe.webpush;

import today.wishwordrobe.presentation.dto.PushNotificationRequest;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

//웹푸시 발송용 dto 공통알림 타이틀, message, data,url,image등을 담음
//서버가 특정 구독 대상에게 웹푸시를 보낼때 사용
//무엇을 보낼지 어떻게 보낼지
@Getter
@Setter
@SuperBuilder
public class WebPushNotificationRequest extends PushNotificationRequest {
    //웹 푸시 전용 필드
    private WebPushSubscription subscription;


    public WebPushNotificationRequest(String title, String message, String icon, String clickAction,
                                      Map<String, String> data, String url, String image,
                                      WebPushSubscription subscription) {
        super(title, message, icon, clickAction, data, url,image);
        this.subscription = subscription;
    }
}
