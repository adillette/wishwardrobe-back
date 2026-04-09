package today.wishwordrobe.test;

import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
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
            
            System.out.println("\n========================================");
            System.out.println("=== 전송 시작 ===");
            System.out.println("========================================\n");
            
            HttpResponse response = pushService.send(notification);
            
            int statusCode = response.getStatusLine().getStatusCode();
            String reasonPhrase = response.getStatusLine().getReasonPhrase();
            
            System.out.println("\n========================================");
            System.out.println("=== 전송 완료 (예외 안 던짐) ===");
            System.out.println("Status Code: " + statusCode);
            System.out.println("Reason: " + reasonPhrase);
            System.out.println("========================================\n");
            
            // 결론
            if (statusCode == 410) {
                System.out.println("✅ 결론: 410 Gone 응답이 왔지만 예외를 던지지 않음");
                System.out.println("✅ HttpResponse 객체로 반환됨");
            } else if (statusCode >= 400) {
                System.out.println("✅ 결론: " + statusCode + " 응답이 왔지만 예외를 던지지 않음");
                System.out.println("✅ HttpResponse 객체로 반환됨");
            } else {
                System.out.println("의외의 성공 응답: " + statusCode);
            }
            
        } catch (IOException e) {
            System.out.println("\n========================================");
            System.out.println("=== IOException 발생 ===");
            System.out.println("메시지: " + e.getMessage());
            System.out.println("========================================\n");
            
            System.out.println("❌ 결론: 에러 응답 시 IOException을 던짐");
            
            if (e.getMessage() != null && e.getMessage().contains("410")) {
                System.out.println("❌ IOException 메시지에 '410' 포함됨");
            }
            
            e.printStackTrace();
            
        } catch (Exception e) {
            System.out.println("\n========================================");
            System.out.println("=== 기타 예외 발생 ===");
            System.out.println("예외 타입: " + e.getClass().getName());
            System.out.println("메시지: " + e.getMessage());
            System.out.println("========================================\n");
            
            e.printStackTrace();
        }
    }
}