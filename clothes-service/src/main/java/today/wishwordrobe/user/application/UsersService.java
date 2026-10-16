package today.wishwordrobe.user.application;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import today.wishwordrobe.user.domain.Users;
import today.wishwordrobe.user.infrastructure.UserRepository;
import today.wishwordrobe.user.presentation.dto.request.UsersLoginRequest;
import today.wishwordrobe.user.presentation.dto.request.UsersRegisterRequest;
import today.wishwordrobe.user.presentation.dto.request.UsersUpdateRequest;
import today.wishwordrobe.user.presentation.dto.response.UsersResponse;

@RequiredArgsConstructor 
@Service
public class UsersService {
     private final UserRepository userRepository;
    // private final UsersDao usersDao;

   //회원가입
    @Transactional 
    public int register(UsersRegisterRequest request) {
        //아이디 중복 체크
         Users users = userRepository.findByLoginId(request.getLoginId());
         
         if(users != null) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
         }
         users = Users.builder()
                .userName(request.getUserName())
                .userEmail(request.getUserEmail())
                .loginId(request.getLoginId())
                .address(request.getAddress())
                .password(request.getPassword())
                .phone(request.getPhone())
                .build();

         userRepository.save(users);
         return 1; //회원가입 성공 시 1 반환

        
    }

   //로그인
   public UsersResponse login(UsersLoginRequest request) {
      Users users = userRepository.findByLoginId(request.getLoginId());
        if(users==null) {
             throw new IllegalArgumentException("존재하지 않는 아이디입니다.");
        }

        if(!users.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        UsersResponse response = new UsersResponse();
        response.setUserId(users.getUserId());
        response.setUserName(users.getUserName()); 
        response.setUserEmail(users.getUserEmail());
        response.setLoginId(users.getLoginId());
        response.setAddress(users.getAddress());
        response.setPhone(users.getPhone());
        response.setPassword(users.getPassword());


        return response;
    }
   //회원정보 수정
    @Transactional 
    public int updateUsers(Long userId, UsersUpdateRequest request) {
        
        //기존 회원 찾기
        Users users = userRepository.findByUserId(userId);
        if(users==null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
       
        //이름 변경
        if(request.getUserName()!=null && !request.getUserName().equals(users.getUserName())) {
            users.setUserName(request.getUserName());
            
        }

        //이메일 변경
        if(request.getUserEmail()!=null && !request.getUserEmail().equals(users.getUserEmail())) {
            users.setUserEmail(request.getUserEmail());
        }
        
        //주소 변경
        if(request.getAddress()!=null && !request.getAddress().equals(users.getAddress())) {
            users.setAddress(request.getAddress());
        }
       

        //비밀번호 변경
        if(request.getPassword()!=null && !request.getPassword().equals(users.getPassword())) {
            users.setPassword(request.getPassword());
        }
        
        //핸드폰 번호 변경
        if(request.getPhone()!=null && !request.getPhone().equals(users.getPhone())) {
            users.setPhone(request.getPhone());
        }
        
      
        userRepository.save(users);
        return 1; //회원정보 수정 성공 시 1 반환
    }

}
