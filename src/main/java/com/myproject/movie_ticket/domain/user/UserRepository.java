package com.myproject.movie_ticket.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 💡 실무 팁: 데이터가 없을 수도 있는 단건 조회는 무조건 Optional로 감싸서 반환합니다. (NullPointerException 방지)
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크용 (결과를 true/false로 반환)
    boolean existsByEmail(String email);

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllUsersIncludingDeleted();
}
