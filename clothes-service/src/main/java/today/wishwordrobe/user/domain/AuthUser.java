package today.wishwordrobe.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor 
public class AuthUser {
   private final Long userId;
private final String loginId;
}
