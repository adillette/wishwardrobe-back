package today.wishwordrobe.user.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@AllArgsConstructor
@NoArgsConstructor
public class UsersResponse {
    private String jwt;
    private Long userId;
    private String userName;
    private String userEmail;
    private String loginId;
    private String address;
    
    private String phone;
}
