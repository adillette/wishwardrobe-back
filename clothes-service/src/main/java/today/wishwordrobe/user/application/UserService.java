package today.wishwordrobe.user.application;

import today.wishwordrobe.domain.AddUserRequest;
import today.wishwordrobe.user.domain.Users;
import today.wishwordrobe.user.infrastructure.UserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public Long save(AddUserRequest dto){
        // BCrypt로 비밀번호 해싱 (cost factor 12)
        String hashedPassword = BCrypt.withDefaults().hashToString(12, dto.getPassword().toCharArray());
        return userRepository.save(
                Users.builder()
                .email(dto.getEmail())
                .password(hashedPassword)
                .phone(dto.getPhone())
                .build()
        ).getId();
    }

    public Users findById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("Unexpected user"));
    }
}
