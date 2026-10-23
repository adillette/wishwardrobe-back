package today.wishwordrobe.user.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import today.wishwordrobe.user.application.JwtService;

@Component 
@Slf4j
@RequiredArgsConstructor 
public class LoginCheckInterceptor implements HandlerInterceptor{
  public static final String ATTR_AUTH_USER = "authUser";
  private final JwtService jwtService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
   
    // Authorization: Bearer xxxxxx
    
    String authorization = request.getHeader("Authorization");
    
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"JWT가 없습니다.");
      return false; //prehandle의 반환값 여기서 중단하라는 뜻
      // 토큰 없는 요청 막기 위해서
    }

    String jwt= authorization.substring(7);//"Bearer "까지가 7글자여서 
    
    request.setAttribute(ATTR_AUTH_USER, jwtService.parseUser(jwt));
    return true; // 다음 단계로 진행 인터셉터-> 컨트롤러 실행
    }

  }


