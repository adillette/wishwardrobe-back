package today.wishwordrobe.infrastructure;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "notification_preferences")
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class NotificationPreferenceDocument {
 @Id
    private Long userId;               // 유저당 1개라서 PK, save가 곧 upsert
    private String preferredChannel;   // "FCM" | "WEBPUSH"
    private LocalDateTime updatedAt;
}
