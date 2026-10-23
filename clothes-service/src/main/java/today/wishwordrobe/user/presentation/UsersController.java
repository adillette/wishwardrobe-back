package today.wishwordrobe.user.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import today.wishwordrobe.user.application.UsersService;
import today.wishwordrobe.user.domain.AuthUser;
import today.wishwordrobe.user.interceptor.LoginCheckInterceptor;
import today.wishwordrobe.user.presentation.dto.request.PasswordChangeRequest;
import today.wishwordrobe.user.presentation.dto.request.PasswordVerifyRequest;
import today.wishwordrobe.user.presentation.dto.request.UsersLoginRequest;
import today.wishwordrobe.user.presentation.dto.request.UsersRegisterRequest;
import today.wishwordrobe.user.presentation.dto.request.UsersUpdateRequest;
import today.wishwordrobe.user.presentation.dto.response.UsersResponse;


@RequiredArgsConstructor
@RequestMapping("/users")
@RestController
public class UsersController {
   
    private final UsersService userService;

    //회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UsersRegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<UsersResponse> login(@RequestBody UsersLoginRequest request) {
        UsersResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    //회원정보 수정
    @PutMapping ("/update")
    public ResponseEntity<String> updateUsers(
        @RequestAttribute("authUser") AuthUser authUser, // 토큰에서 나온 사용자
        @RequestBody @Valid UsersUpdateRequest request) {
        userService.updateUsers(authUser.getUserId(),request);
        return ResponseEntity.ok("회원정보 수정 성공");
    }

    //비밀번호 확인 수정화면 진입용
    @PostMapping("/verify-password")
    public ResponseEntity<Void> verifyPassword(
        @RequestAttribute("authUser") AuthUser authUser,
        @RequestBody @Valid PasswordVerifyRequest request) {
    userService.verifyPassword(authUser.getUserId(), request.getPassword());
    return ResponseEntity.noContent().build();
    }
    
    //비밀번호 수정
    @PutMapping("/updatepw")
    public ResponseEntity<Void> changePassword(
        @RequestAttribute("authUser") AuthUser authUser, // 누구인지는 토큰에서 온다
        @RequestBody @Valid PasswordChangeRequest request) {
    userService.changePassword(authUser.getUserId(), request);
    return ResponseEntity.noContent().build();
}

}
