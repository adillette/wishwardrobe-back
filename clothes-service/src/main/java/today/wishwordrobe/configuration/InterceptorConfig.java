package today.wishwordrobe.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import today.wishwordrobe.user.interceptor.LoginCheckInterceptor;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor 
public class InterceptorConfig implements WebMvcConfigurer{
     
    private final LoginCheckInterceptor loginCheckInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loginCheckInterceptor)
        .addPathPatterns("/api/**") // /api 아래의 모든 요청에 토큰 검사를 건다
        .excludePathPatterns(
            "/api/users/register", // 가입은 토큰이 없는 사람이 호출하므로 검사에서 뺀다
            "/api/users/login"   // 로그인도 마찬가지다. 여기서 토큰을 처음 발급받는다
        );
  }
  @Bean
  public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder(); // 가입, 수정, 로그인에서 같은 인스턴스를 씁니다
    }
}

