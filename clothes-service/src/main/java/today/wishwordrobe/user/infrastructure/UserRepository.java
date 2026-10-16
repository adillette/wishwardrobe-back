package today.wishwordrobe.user.infrastructure;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import today.wishwordrobe.user.domain.Users;

import java.util.Optional;
import java.util.List;


@Repository 
public interface UserRepository extends JpaRepository<Users, Long> {
    //로그인 등 조회후 값을 쓸때
    Optional<Users> findByUserEmail(String userEmail);
    Users findByUserId(Long userId);
    Users findByLoginId(String loginId);



}
