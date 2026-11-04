package today.wishwordrobe.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor //프론트 요청 수신용
public class SendNowRequest {
  private double lat;
  private double lon;
}
