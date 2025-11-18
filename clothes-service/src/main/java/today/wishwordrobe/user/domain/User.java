package today.wishwordrobe.user.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import jakarta.persistence.*;

@Table(name = "users")
@NoArgsConstructor
@Getter
@Entity
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String email;
    private String password;
    private String phone;

    @Builder
    public User(String email,String password, String phone){
        this.email = email;
        this.password = password;
        this.phone = phone;
    }
//public User(String email,String password, String phone,String auth){


//사용자에게 "user" 권한을 부여한다
// 이 메서드로 특정 url이나 기능에 접근 가능한지 확인
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; //이메일을 로그인 식별자로 사용하는것을 의미
    }
//계정이 만료되지 않았는지 확인
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
//계정이 잠기지 않았는지 확인 - 여러번 로그인 실패
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
//자격 증명이 만료되지 않았는지 확인
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
//계정이 활성화 되어있는지 확인
    @Override
    public boolean isEnabled() {
        return true;
    }


}
