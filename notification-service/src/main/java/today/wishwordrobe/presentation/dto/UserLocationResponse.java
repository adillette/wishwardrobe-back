package today.wishwordrobe.presentation.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationResponse {

  private long userId;
  private double lat;
  private double lon;
  private boolean enabled;
  private LocalDateTime updatedAt;
}
