package today.wishwordrobe.user.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import today.wishwordrobe.configuration.PasswordMismatchException;
import today.wishwordrobe.configuration.UnauthorizedException;
import today.wishwordrobe.user.domain.Users;
import today.wishwordrobe.user.infrastructure.UserRepository;
import today.wishwordrobe.user.presentation.dto.request.PasswordChangeRequest;
import today.wishwordrobe.user.presentation.dto.request.UsersLoginRequest;
import today.wishwordrobe.user.presentation.dto.request.UsersRegisterRequest;
import today.wishwordrobe.user.presentation.dto.request.UsersUpdateRequest;
import today.wishwordrobe.user.presentation.dto.response.UsersResponse;

@RequiredArgsConstructor 
@Service
public class UsersService {
     private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
   //회원가입
    @Transactional 
    public String register(UsersRegisterRequest request) {
        //아이디 중복 체크
         Users users = userRepository.findByLoginId(request.getLoginId());
         
         if(users != null) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
         }
         String encoded = passwordEncoder.encode(request.getPassword());
         users = Users.builder()
                .userName(request.getUserName())
                .userEmail(request.getUserEmail())
                .loginId(request.getLoginId())
                .address(request.getAddress())
                .password(encoded)
                .phone(request.getPhone())
                .build();

      

        userRepository.save(users);
        
        return "회원가입 완료";

        
    }

   //로그인
   @Transactional(readOnly = true)
   public UsersResponse login(UsersLoginRequest request) {
      Users users = userRepository.findByLoginId(request.getLoginId());
       if (users == null || !passwordEncoder.matches(request.getPassword(), users.getPassword())) {
        throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
    }
        String jwt = jwtService.createJwt(users.getUserId(),users.getLoginId());
        
       return UsersResponse.builder()
               .jwt(jwt)
            .userId(users.getUserId())
            .userName(users.getUserName())
            .userEmail(users.getUserEmail())
            .loginId(users.getLoginId())  
            .address(users.getAddress())
            .phone(users.getPhone())      
            .build();
    }

    //현재 비밀 번호 확인하기
    @Transactional(readOnly = true)
    public void verifyPassword(Long userId, String rawPassword){
        if (!StringUtils.hasText(rawPassword)) { // 비어 있으면 matches까지 가지 않고 불일치 처리
        throw new PasswordMismatchException("비밀번호를 입력해주세요");
        }
        Users users = getLoginUser(userId);

         if (!passwordEncoder.matches(rawPassword, users.getPassword())) {
            throw new PasswordMismatchException("비밀번호가 재확인 실패");
        }
    }

     private Users getLoginUser(Long userId) {
        Users users = userRepository.findByUserId(userId);
        if (users == null) {
            throw new UnauthorizedException("사용자를 찾을 수 없습니다.");
        }
        return users;
    }


   //회원정보 수정
    @Transactional 
    public String updateUsers(Long userId, UsersUpdateRequest request) {
        
        //기존 회원 찾기
        Users users = userRepository.findByUserId(userId);
        if(users==null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
       if (StringUtils.hasText(request.getUserName())) 
        users.setUserName(request.getUserName());
        if (StringUtils.hasText(request.getUserEmail())) 
            users.setUserEmail(request.getUserEmail());
        if (StringUtils.hasText(request.getAddress()))   
            users.setAddress(request.getAddress());
        if (StringUtils.hasText(request.getPhone()))     
            users.setPhone(request.getPhone());
       
        userRepository.save(users);
        return "회원정보 수정 성공"; 
    }

    //비밀번호 변경
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        Users users = getLoginUser(userId);
 
        if (!passwordEncoder.matches(request.getCurrentPassword(), users.getPassword())) {
            throw new PasswordMismatchException("비밀번호 재확인 실패 올바른 비밀번호를 입력해주세요");
        }
 
        String newPassword = request.getNewPassword();
        
 
        users.setPassword(passwordEncoder.encode(newPassword));
    }





}
