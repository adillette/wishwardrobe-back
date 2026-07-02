package today.wishwordrobe.infrastructure;

import java.time.LocalDateTime;

import org.checkerframework.checker.index.qual.IndexFor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "user_locations")
@Getter
@Setter
@NoArgsConstructor
public class UserLocationDocument {
  @Id
  private String id;

  @Indexed(unique=true)
  private long userId;

  // 사용자가 저장한 위치
    private double lat;
    private double lon;

    // 알림 수신 동의 여부
    // false면 스케줄러에서 skip
    private boolean enabled;

     private LocalDateTime updatedAt;

    @Builder
    public UserLocationDocument(long userId, double lat, double lon,
                                 boolean enabled, LocalDateTime updatedAt) {
        this.userId = userId;
        this.lat = lat;
        this.lon = lon;
        this.enabled = enabled;
        this.updatedAt = updatedAt;
    }

}
