package com.mobilebanking.platform.auth.service;

import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.common.util.Hashing;
import com.mobilebanking.platform.config.BankProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "bank.security.redis-rate-limit-enabled", havingValue = "true")
public class RedisLoginRateLimiter implements LoginRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final BankProperties properties;

    @Override
    public void checkAllowed(String username, String ip) {
        try {
            String value = redisTemplate.opsForValue().get(key(username, ip));
            int failures = value == null ? 0 : Integer.parseInt(value);
            if (failures >= properties.getSecurity().getLoginMaxFailures()) {
                throw new BusinessException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
            }
        } catch (RedisConnectionFailureException exception) {
            throw new BusinessException(ErrorCode.SERVICE_UNAVAILABLE, "登录风控服务暂不可用");
        }
    }

    @Override
    public void recordFailure(String username, String ip) {
        try {
            String key = key(username, ip);
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                Duration window = properties.getSecurity().getLoginWindow();
                redisTemplate.expire(key, window);
            }
        } catch (RedisConnectionFailureException exception) {
            throw new BusinessException(ErrorCode.SERVICE_UNAVAILABLE, "登录风控服务暂不可用");
        }
    }

    @Override
    public void clear(String username, String ip) {
        try {
            redisTemplate.delete(key(username, ip));
        } catch (RedisConnectionFailureException ignored) {
            // 登录成功后清理失败不影响主流程，键会按 TTL 自动过期。
        }
    }

    private String key(String username, String ip) {
        return "mobile-bank:login:" + Hashing.sha256(username.toLowerCase() + "|" + ip);
    }
}
