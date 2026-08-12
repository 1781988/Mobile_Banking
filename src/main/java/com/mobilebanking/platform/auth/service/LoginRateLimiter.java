package com.mobilebanking.platform.auth.service;

public interface LoginRateLimiter {
    void checkAllowed(String username, String ip);
    void recordFailure(String username, String ip);
    void clear(String username, String ip);
}
