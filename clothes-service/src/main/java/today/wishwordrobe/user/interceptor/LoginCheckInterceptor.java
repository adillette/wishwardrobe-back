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

    if (!(handler instanceof HandlerMethod)) {
      // 정적 리소스나 다른 핸들러는 통과
      return true;
    }

    HandlerMethod handlerMethod = (HandlerMethod) handler;
    

    
      String jwt=null;

      String authorization = request.getHeader("Authorization");
      if (authorization != null) {
        // Authorization: Bearer xxxxxx
        if (!authorization.substring(7).equals("")) {
          jwt = authorization.substring(7);
        }
      }

      if (jwt == null) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "JWT가 없습니다.");
        return false;
      } else {
        if (jwtService.validateJwt(jwt)) {
          return true;
        } else {
          response.sendError(HttpServletResponse.SC_FORBIDDEN, "JWT가 유효하지 않습니다.");
          return false;
        }
      }
    }

  }


