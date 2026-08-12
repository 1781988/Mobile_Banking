package com.mobilebanking.platform.auth.service;

import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.config.BankProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "bank.security.redis-rate-limit-enabled", havingValue = "false", matchIfMissing = true)
public class LocalLoginRateLimiter implements LoginRateLimiter {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final BankProperties properties;
    private final Clock clock;

    public LocalLoginRateLimiter(BankProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public void checkAllowed(String username, String ip) {
        String key = key(username, ip);
        Window window = windows.get(key);
        if (window == null) {
            return;
        }
        Instant now = clock.instant();
        if (window.expiresAt().isBefore(now)) {
            windows.remove(key, window);
            return;
        }
        if (window.failures() >= properties.getSecurity().getLoginMaxFailures()) {
            throw new BusinessException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }
    }

    @Override
    public void recordFailure(String username, String ip) {
        Instant now = clock.instant();
        windows.compute(key(username, ip), (ignored, existing) -> {
            if (existing == null || existing.expiresAt().isBefore(now)) {
                return new Window(1, now.plus(properties.getSecurity().getLoginWindow()));
            }
            return new Window(existing.failures() + 1, existing.expiresAt());
        });
    }

    @Override
    public void clear(String username, String ip) {
        windows.remove(key(username, ip));
    }

    private String key(String username, String ip) {
        return username.toLowerCase() + "|" + ip;
    }

    private record Window(int failures, Instant expiresAt) {
    }
}
