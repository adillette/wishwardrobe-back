package today.wishwordrobe.notification;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import com.google.auto.value.AutoValue.Builder;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@Slf4j
public class WebpushNotificationsubscription {

  @Id
  private Long id;

  private String userId;
  private String endpoint;
  private Keys keys;
  private LocalDateTime createdAt;
  private LocalDateTime lastUsedAt;
  private boolean isActive;

  @Data
  @Builder
  public static class Keys{
    private String p256h;
    private String auth;
  }
}
