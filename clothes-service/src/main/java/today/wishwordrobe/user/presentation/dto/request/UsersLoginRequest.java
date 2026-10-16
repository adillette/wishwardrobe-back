package today.wishwordrobe.user.presentation.dto.request;

import lombok.Data;

@Data 
public class UsersLoginRequest {
    private String loginId;
    private String password;
}
