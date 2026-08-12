package com.mobilebanking.platform.auth.repository;

import com.mobilebanking.platform.auth.domain.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {

    @Query("select s from AuthSession s join fetch s.user where s.tokenHash = :tokenHash")
    Optional<AuthSession> findWithUserByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("update AuthSession s set s.revokedAt = :revokedAt where s.tokenHash = :tokenHash and s.revokedAt is null")
    int revokeByTokenHash(@Param("tokenHash") String tokenHash, @Param("revokedAt") Instant revokedAt);

    long deleteByExpiresAtBefore(Instant threshold);
}
