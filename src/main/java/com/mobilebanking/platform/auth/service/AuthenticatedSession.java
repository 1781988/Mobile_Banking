package com.mobilebanking.platform.auth.service;

import com.mobilebanking.platform.auth.security.BankingPrincipal;

public record AuthenticatedSession(
        BankingPrincipal principal,
        Long sessionId
) {
}
