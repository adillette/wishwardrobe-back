package today.wishwordrobe.auth;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component 
@RequiredArgsConstructor 
@Slf4j 
public class JwtAuthWebFilter implements WebFilter{
    public static final String ATTR_AUTH_USER = "authUser";
    private final JwtAuthParser jwtAuthParser;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
       String path = exchange.getRequest().getURI().getPath();

       boolean protectedPath = path.contains("/user-location") || path.contains("/notification");
       
       
       if (!protectedPath || path.endsWith("/public-key")) {
            return chain.filter(exchange);
        }

       //CORS preflight는 통과
       if(HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())){
        return chain.filter(exchange);
       }

       String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
       if(authorization==null || !authorization.startsWith("Bearer ")){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
       }

       try{
        AuthUser authUser = jwtAuthParser.parseUser(authorization.substring(7));
        exchange.getAttributes().put(ATTR_AUTH_USER, authUser);
       } catch (Exception e) {
            log.info("JWT 검증 실패: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
       }
        return chain.filter(exchange);
    }

    

}
