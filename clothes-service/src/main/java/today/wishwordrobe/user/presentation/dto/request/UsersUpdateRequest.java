package today.wishwordrobe.user.presentation.dto.request;

import lombok.Data;

@Data 
public class UsersUpdateRequest {
    private Long userId;
    private String userName;
    private String userEmail;
    private String address;
    private String phone;
    
}
