package com.mobilebanking.platform.auth.service;

import com.mobilebanking.platform.audit.domain.AuditResult;
import com.mobilebanking.platform.audit.service.AuditService;
import com.mobilebanking.platform.auth.security.BankingPrincipal;
import com.mobilebanking.platform.auth.web.LoginRequest;
import com.mobilebanking.platform.auth.web.LoginResponse;
import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.user.domain.AppUser;
import com.mobilebanking.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DUMMY_BCRYPT_HASH =
            "$2a$10$LrRPuCcPO99mzwtp0dR8ZuEh/T/NfXrK8T2OVJDSA9i9vPtin35za";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginRateLimiter loginRateLimiter;
    private final AuditService auditService;

    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        String normalizedUsername = request.username().trim().toLowerCase();
        loginRateLimiter.checkAllowed(normalizedUsername, ip);

        AppUser user = userRepository.findByUsernameIgnoreCaseAndEnabledTrue(normalizedUsername).orElse(null);
        String candidateHash = user == null ? DUMMY_BCRYPT_HASH : user.getPasswordHash();
        boolean passwordMatches = passwordEncoder.matches(request.password(), candidateHash);
        if (user == null || !passwordMatches) {
            loginRateLimiter.recordFailure(normalizedUsername, ip);
            auditService.record(user == null ? null : user.getId(), "AUTH_LOGIN", "USER",
                    user == null ? normalizedUsername : String.valueOf(user.getId()),
                    AuditResult.FAILURE, ip, "invalid_credentials");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        loginRateLimiter.clear(normalizedUsername, ip);
        IssuedToken issuedToken = tokenService.issue(user, ip, userAgent);
        auditService.record(user.getId(), "AUTH_LOGIN", "USER", String.valueOf(user.getId()),
                AuditResult.SUCCESS, ip, "login_success");
        return new LoginResponse(
                "Bearer",
                issuedToken.rawToken(),
                issuedToken.expiresAt(),
                BankingPrincipal.from(user)
        );
    }

    public void logout(BankingPrincipal principal, String rawToken, String ip) {
        tokenService.revoke(rawToken);
        auditService.record(principal.userId(), "AUTH_LOGOUT", "USER", String.valueOf(principal.userId()),
                AuditResult.SUCCESS, ip, "logout_success");
    }
}
