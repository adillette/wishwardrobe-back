package today.wishwordrobe.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//토큰 관리용 dto 토큰 등록, 삭제,해제할때 사용 어디로 보낼지 저장
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequest {
    
    private String userId;      // 필수: 토큰 소유자
    private String token;       // 필수: FCM 토큰
    private String deviceId;    // 선택: 디바이스 식별자 (같은 유저의 여러 디바이스 구분)
}
