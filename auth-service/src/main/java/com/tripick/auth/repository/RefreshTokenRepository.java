package com.tripick.auth.repository;

import com.tripick.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserIdAndToken(Long userId, String token);

    void deleteByUserId(Long userId);

    void deleteByToken(String token);
}
