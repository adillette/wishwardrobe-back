package today.wishwordrobe.presentation.dto;

import lombok.Data;

@Data
public class NotificationPreferenceRequest {
    private Long userId;
    private String channel; 
}
