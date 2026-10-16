package today.wishwordrobe.user.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import today.wishwordrobe.user.application.UsersService;
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
    public ResponseEntity<String> updateUsers(@RequestParam ("userId") Long userId
                                ,@RequestBody UsersUpdateRequest request) {
        userService.updateUsers(userId,request);
        return ResponseEntity.ok("회원정보 수정 성공");
    }

}
