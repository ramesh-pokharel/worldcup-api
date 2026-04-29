package com.worldcup.domain.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUserIdAndTokenAndUsedFalse(Long userId, String token);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.userId = :userId")
    void deleteByUserId(Long userId);
}
