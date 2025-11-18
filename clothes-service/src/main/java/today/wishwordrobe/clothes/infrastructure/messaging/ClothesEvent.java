package today.wishwordrobe.clothes.infrastructure.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import today.wishwordrobe.clothes.domain.ClothingCategory;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RabbitMQ를 통해 전송되는 옷장 관련 이벤트
 * 다른 서비스에서 옷장 변경 사항을 비동기로 처리할 수 있도록 함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothesEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 이벤트 타입
     */
    public enum EventType {
        CREATED,    // 옷 추가
        UPDATED,    // 옷 수정
        DELETED     // 옷 삭제
    }

    private EventType eventType;
    private Long clothesId;
    private Long userId;
    private ClothingCategory category;
    private LocalDateTime eventTime;
    private String description;
}
