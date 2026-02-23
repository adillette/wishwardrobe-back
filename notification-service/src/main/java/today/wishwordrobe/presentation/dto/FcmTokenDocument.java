package today.wishwordrobe.presentation.dto;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//DTO/도메인 객체와 DB 저장 객체를 분리하기 위한 Mongo 전용 모델
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

   
    @Field("isActive")
    @Builder.Default
    private Boolean isActive=true;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}
