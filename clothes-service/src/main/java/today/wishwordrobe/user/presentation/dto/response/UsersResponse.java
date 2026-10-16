package today.wishwordrobe.user.presentation.dto.response;

import lombok.Data;

@Data 
public class UsersResponse {
    private Long userId;
    private String userName;
    private String userEmail;
    private String loginId;
    private String address;
    private String password;   
    private String phone;
    
}
