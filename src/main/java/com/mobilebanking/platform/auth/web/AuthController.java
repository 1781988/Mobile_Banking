package com.mobilebanking.platform.auth.web;

import com.mobilebanking.platform.auth.security.BankingPrincipal;
import com.mobilebanking.platform.auth.service.AuthService;
import com.mobilebanking.platform.common.api.ApiResponse;
import com.mobilebanking.platform.common.web.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(
                request,
                ClientIpResolver.resolve(httpRequest),
                httpRequest.getHeader(HttpHeaders.USER_AGENT)
        );
        return ApiResponse.ok(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication, HttpServletRequest request) {
        BankingPrincipal principal = (BankingPrincipal) authentication.getPrincipal();
        String rawToken = String.valueOf(authentication.getCredentials());
        authService.logout(principal, rawToken, ClientIpResolver.resolve(request));
        return ApiResponse.ok(null);
    }
}
