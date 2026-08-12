package com.mobilebanking.platform.auth.service;

import java.time.Instant;

public record IssuedToken(String rawToken, Instant expiresAt) {
}
