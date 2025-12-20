package today.wishwordrobe.user.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;

@Data
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int userId;
    private String userLoginId;
    
    @JsonProperty(access=JsonProperty.Access.WRITE_ONLY)
    private String userPassword;
    private String userName;
    private String userEmail;



}
