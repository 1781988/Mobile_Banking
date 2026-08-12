package com.mobilebanking.platform.auth.service;

import com.mobilebanking.platform.auth.domain.AuthSession;
import com.mobilebanking.platform.auth.repository.AuthSessionRepository;
import com.mobilebanking.platform.auth.security.BankingPrincipal;
import com.mobilebanking.platform.common.util.Hashing;
import com.mobilebanking.platform.config.BankProperties;
import com.mobilebanking.platform.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthSessionRepository authSessionRepository;
    private final BankProperties bankProperties;
    private final Clock clock;

    @Transactional
    public IssuedToken issue(AppUser user, String ip, String userAgent) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant now = clock.instant();
        Instant expiresAt = now.plus(bankProperties.getSecurity().getTokenTtl());

        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setTokenHash(Hashing.sha256(rawToken));
        session.setCreatedAt(now);
        session.setExpiresAt(expiresAt);
        session.setCreatedIp(trim(ip, 64));
        session.setUserAgent(trim(userAgent, 255));
        authSessionRepository.save(session);
        return new IssuedToken(rawToken, expiresAt);
    }

    @Transactional(readOnly = true)
    public Optional<AuthenticatedSession> authenticate(String rawToken) {
        if (rawToken == null || rawToken.isBlank() || rawToken.length() > 256) {
            return Optional.empty();
        }
        Instant now = clock.instant();
        return authSessionRepository.findWithUserByTokenHash(Hashing.sha256(rawToken))
                .filter(session -> session.isActive(now))
                .map(session -> new AuthenticatedSession(BankingPrincipal.from(session.getUser()), session.getId()));
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            authSessionRepository.revokeByTokenHash(Hashing.sha256(rawToken), clock.instant());
        }
    }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void deleteExpiredSessions() {
        authSessionRepository.deleteByExpiresAtBefore(clock.instant().minus(7, ChronoUnit.DAYS));
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
