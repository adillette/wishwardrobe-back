package today.wishwordrobe.auth;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component 
public class JwtAuthParser {
    private final SecretKey secretKey;
    
    public JwtAuthParser(@Value ("${jwt.secret}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public AuthUser parseUser(String jwt){
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(jwt).getPayload();
                return new AuthUser(Long.parseLong(claims.getSubject()), claims.get("loginId", String.class));
    
    }


}
