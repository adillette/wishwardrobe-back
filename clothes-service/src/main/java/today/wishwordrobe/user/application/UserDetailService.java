package today.wishwordrobe.user.application;


import today.wishwordrobe.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailService  {

    private final UserRepository userRepository;


 
}
