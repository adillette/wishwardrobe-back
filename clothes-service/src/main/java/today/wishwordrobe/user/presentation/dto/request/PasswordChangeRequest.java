package today.wishwordrobe.user.presentation.dto.request;

import lombok.Data;

@Data 
public class PasswordChangeRequest {
    private String currentPassword;
    private String newPassword;
}
