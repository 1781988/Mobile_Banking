package com.mobilebanking.platform.auth.service;

import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.config.BankProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalLoginRateLimiterTest {

    @Test
    void shouldBlockAfterConfiguredFailures() {
        BankProperties properties = new BankProperties();
        properties.getSecurity().setLoginMaxFailures(2);
        properties.getSecurity().setLoginWindow(Duration.ofMinutes(5));
        var limiter = new LocalLoginRateLimiter(
                properties,
                Clock.fixed(Instant.parse("2026-08-12T00:00:00Z"), ZoneOffset.UTC));

        limiter.recordFailure("alice", "127.0.0.1");
        limiter.recordFailure("alice", "127.0.0.1");

        assertThatThrownBy(() -> limiter.checkAllowed("alice", "127.0.0.1"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS));
    }
}
