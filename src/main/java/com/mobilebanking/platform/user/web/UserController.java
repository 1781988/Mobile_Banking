package com.mobilebanking.platform.user.web;

import com.mobilebanking.platform.auth.security.BankingPrincipal;
import com.mobilebanking.platform.common.api.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public ApiResponse<BankingPrincipal> me(@AuthenticationPrincipal BankingPrincipal principal) {
        return ApiResponse.ok(principal);
    }
}
