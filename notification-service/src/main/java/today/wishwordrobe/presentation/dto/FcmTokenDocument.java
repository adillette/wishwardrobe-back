package today.wishwordrobe.presentation.dto;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FCM 토큰 저장 Document
 * - token을 @Id로 사용해 중복 방지
 * - userId로 "로그아웃 시 해당 사용자의 모든 토큰 삭제" 가능
 * - deviceId로 "같은 유저의 여러 디바이스" 구분
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "fcm_tokens")
public class FcmTokenDocument {

    @Id
    private String token;  // FCM 토큰 자체가 PK (중복 방지)

    @Indexed
    private String userId;  // 이 토큰이 어느 사용자에게 속하는지

    private String deviceId;  // 선택: 같은 유저의 여러 디바이스 구분

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastUsedAt;
}
