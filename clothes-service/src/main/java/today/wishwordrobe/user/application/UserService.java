package today.wishwordrobe.user.application;

import today.wishwordrobe.domain.AddUserRequest;
import today.wishwordrobe.user.domain.User;


import today.wishwordrobe.user.infrastructure.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Long save(AddUserRequest dto){
        return userRepository.save(
                User.builder()
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .build()
        ).getId();
    }

    public User findById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("Unexpected user"));
    }
}
