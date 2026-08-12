package com.mobilebanking.platform.auth.web;

import com.mobilebanking.platform.auth.security.BankingPrincipal;

import java.time.Instant;

public record LoginResponse(
        String tokenType,
        String accessToken,
        Instant expiresAt,
        BankingPrincipal user
) {
}
