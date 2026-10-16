package today.wishwordrobe.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {
    @Id  
    private Long userId;
    private String userName;
    private String userEmail;
    private String loginId;
    private String address;
    private String password;   
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

   
}
