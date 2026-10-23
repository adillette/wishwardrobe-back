package today.wishwordrobe.user.application;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import today.wishwordrobe.configuration.UnauthorizedException;
import today.wishwordrobe.user.domain.AuthUser;

@Service
@Slf4j
public class JwtService {

  // private String strKey = "com.wishwardrobe-back.secret.key";
  // public JwtService() throws Exception {
  //   byte[] bytes = strKey.getBytes();
  //   secretKey = Keys.hmacShaKeyFor(bytes);
  // }
  private SecretKey secretKey;
  private long jwtDuration = 90 * 24 * 60 *60;

  public JwtService(@Value ("${JWT_SECRET}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String createJwt(Long userId, String loginId) {
    JwtBuilder jwtBuilder = Jwts.builder();
    
    jwtBuilder.subject(String.valueOf(userId));
    jwtBuilder.claim("loginId", loginId);
    

    jwtBuilder.expiration(new Date(new Date().getTime() + jwtDuration));

    jwtBuilder.signWith(secretKey);

    String jwt = jwtBuilder.compact();

    return jwt;
  }

  public boolean validateJwt(String jwt) {
    boolean result = false;
    try {
      // JWT를 해석하는 JwtParser 얻기
      JwtParserBuilder jwtParserBuilder = Jwts.parser();
      jwtParserBuilder.verifyWith(secretKey);
      JwtParser jwtParser = jwtParserBuilder.build();

      // JWT 해석
      Jws<Claims> jws = jwtParser.parseSignedClaims(jwt);
      result = true;
    } catch (ExpiredJwtException e) {
      log.info("기간이 만료된 토큰입니다.");
    } catch (io.jsonwebtoken.security.SecurityException e) {
      log.info("잘못 서명된 토큰입니다.");
    } catch (Exception e) {
      log.info("토큰을 해석할 수 없습니다.");
    }
    return result;
  }

  public AuthUser parseUser(String jwt){
    try {
    Claims claims = Jwts.parser().verifyWith(secretKey).build()
        .parseSignedClaims(jwt).getPayload();
    return new AuthUser(Long.parseLong(claims.getSubject()), 
        claims.get("loginId", String.class));


  } catch (ExpiredJwtException e) {
      // log.info("기간이 만료된 토큰입니다.");
      throw new UnauthorizedException("기간이 만료된 토큰입니다.");
    } catch (io.jsonwebtoken.security.SecurityException e) {
      // log.info("잘못 서명된 토큰입니다.");
      throw new UnauthorizedException("잘못 서명된 토큰입니다.");
    } catch (Exception e) {
      // log.info("토큰을 해석할 수 없습니다.");
      throw new UnauthorizedException("토큰을 해석할 수 없습니다.");
    }
  }

}
