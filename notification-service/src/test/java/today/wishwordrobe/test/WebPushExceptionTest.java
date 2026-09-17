package today.wishwordrobe.test;

import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Disabled ("실제 FCM 서버로 네트워크 호출하는 수동 검증용 — CI/빌드에서 자동 실행 금지")
@Slf4j 
public class WebPushExceptionTest {
    
    @Autowired
    private PushService pushService;
    
    @Test
    public void test410Response() {
        try {
            // Notification 생성자: (String endpoint, String publicKey, String auth, byte[] payload)
            nl.martijndwars.webpush.Notification notification = 
                new nl.martijndwars.webpush.Notification(
                    "https://fcm.googleapis.com/fcm/send/INVALID_TOKEN_12345",  // endpoint
                    "BDxcO0vK8HjrlGAOHhlCBj75G21uuRlc64WQU/lf1KOKMvZ175sLLcpEPjaln3QvJK9a5idKKegvSjPbslbd8XA=",  // publicKey (String)
                    "OXde8QZMgpvnN+05Dljhgw==",  // auth (Base64 String 그대로)
                    "test message".getBytes()  // payload (byte[])
                );
            
          
            HttpResponse response = pushService.send(notification);
            
            int statusCode = response.getStatusLine().getStatusCode();
            String reasonPhrase = response.getStatusLine().getReasonPhrase();
            log.info("HTTP Status Code: {}", statusCode);
            log.info("reasonPhrase: {}", reasonPhrase);
           
            // 결론
            if (statusCode == 410) {
                log.info("✅ 결론: 410 Gone 응답이 왔지만 예외를 던지지 않음");
                log.info("✅ HttpResponse 객체로 반환됨");
            } else if (statusCode >= 400) {
                log.info("✅ 결론: {} 응답이 왔지만 예외를 던지지 않음", statusCode);
                log.info("✅ HttpResponse 객체로 반환됨");
            } else {
                log.info("의외의 성공 응답: {}", statusCode);
            }
            
        } catch (IOException e) {
            log.error("결론: 에러 응답 시 IOException을 던짐", e);

            if (e.getMessage() != null && e.getMessage().contains("410")) {
                log.error(" IOException 메시지에 '410' 포함됨");
            }
            
            e.printStackTrace();
            
        } catch (Exception e) {
           
            log.error("예외 발생:{}", e.getMessage(), e);
            e.printStackTrace();
        }
    }
}