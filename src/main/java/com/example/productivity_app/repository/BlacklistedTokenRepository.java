package com.example.productivity_app.repository;

import com.example.productivity_app.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    Optional<BlacklistedToken> findByToken(String token);
}
