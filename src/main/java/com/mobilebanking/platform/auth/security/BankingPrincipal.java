package com.mobilebanking.platform.auth.security;

import com.mobilebanking.platform.user.domain.AppUser;
import com.mobilebanking.platform.user.domain.UserRole;

public record BankingPrincipal(
        Long userId,
        String username,
        String displayName,
        UserRole role
) {
    public static BankingPrincipal from(AppUser user) {
        return new BankingPrincipal(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
    }
}
