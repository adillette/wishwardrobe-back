package today.wishwordrobe.user.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig {

    private final UserDetailsService userService;

    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring()
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**"))  // 올바른 H2 콘솔 경로 참조
                .requestMatchers(new AntPathRequestMatcher("/static/**"));
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
         return http
                        .authorizeHttpRequests(auth->auth
                        .requestMatchers(new AntPathRequestMatcher("/login")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/signup")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/user")).hasRole("USER")
                        .anyRequest().authenticated()
                        )
                .formLogin(formLogin->formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        )
                        .logout(logout ->logout
                                .logoutSuccessUrl("/login")
                                .invalidateHttpSession(true)
                            )
                                .csrf(AbstractHttpConfigurer::disable)
                                .build();


    }
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder, UserDetailsService userDetailsService)
            throws Exception{
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return new ProviderManager(authProvider);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
