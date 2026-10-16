package today.wishwordrobe.user.presentation.dto.request;

import lombok.Data;

@Data 
public class UsersRegisterRequest {
 
    
    private String loginId;
    private String userName;
    private String userEmail;
    private String address;
    private String password;   
    private String phone;
   
}
