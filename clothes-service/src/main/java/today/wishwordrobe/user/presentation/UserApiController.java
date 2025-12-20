package today.wishwordrobe.user.presentation;

import today.wishwordrobe.domain.AddUserRequest;
import today.wishwordrobe.user.application.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Controller
public class UserApiController {

    private final UserService userService;

    @PostMapping("/user")
    public String signup(@ModelAttribute AddUserRequest request){
        System.out.println("--------");
        userService.save(request);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response){
      
        return "redirect:/login";
    }

    /*
     @RestController
@RequestMapping("/api/users")
@Slf4j
public class UsersController {
  @Autowired
  private UsersService usersService;

  @Autowired
  private JwtService jwtService;

  @PostMapping("/create")
  public Map<String, Object> create(@RequestBody @Valid UsersSignupRequest request) {
    Map<String, Object> map = new HashMap<>();
    try {
      usersService.create(request);
      map.put("result", "success");
    } catch (Exception e) {
      map.put("result", "fail");
      map.put("message", e.getMessage());
    }
    return map;
  }

  @PostMapping("/login")
  public Map<String, Object> login(@RequestBody UsersLoginRequest request) {
    Map<String, Object> map = new HashMap<>();
    try {
      // 로그인 아이디로 사용자 조회
      Users users = usersService.getUsersByLoginId(request.getUserLoginId());

      // 비밀번호 비교
      PasswordEncoder encoder = new BCryptPasswordEncoder();
      if (!encoder.matches(request.getUserPassword(), users.getUserPassword())) {
        throw new IllegalArgumentException("비밀번호 불일치");
      }

      // JWT 토큰 발급
      String jwt = jwtService.createJwt(users.getUserId(), users.getUserLoginId(), users.getUserEmail());

      // 응답 데이터
      map.put("result", "success");
      map.put("userId", users.getUserId());
      map.put("userLoginId", users.getUserLoginId());
      map.put("jwt", jwt);
    } catch (Exception e) {
      map.put("result", "fail");
      map.put("message", e.getMessage());
    }
    return map;
  }

  @PutMapping("/update")
  public Map<String, Object> update(@ModelAttribute UsersUpdateRequest request) {
    Map<String, Object> map = new HashMap<>();
    try {
      Users updated = usersService.updateProfile(request);
      map.put("result", "success");
      map.put("users", toResponse(updated));
      map.put("tags", request.getTagIds());
    } catch (Exception e) {
      map.put("result", "fail");
      map.put("message", e.getMessage());
    }
    return map;
  }

  @DeleteMapping("/delete")
  public Map<String, Object> delete(@RequestParam("userId") int userId) {
    Map<String, Object> map = new HashMap<>();
    try {
      int rows = usersService.delete(userId);
      if (rows == 0)
        throw new IllegalArgumentException("삭제 실패");
      map.put("result", "success");
      map.put("message", "사용자가 삭제되었습니다.");
    } catch (Exception e) {
      map.put("result", "fail");
      map.put("message", e.getMessage());
    }
    return map;
  }
     */


}
