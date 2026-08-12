package com.mobilebanking.platform.account.web;

import com.mobilebanking.platform.account.service.AccountQueryService;
import com.mobilebanking.platform.auth.security.BankingPrincipal;
import com.mobilebanking.platform.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountQueryService accountQueryService;

    @GetMapping
    public ApiResponse<List<AccountResponse>> list(@AuthenticationPrincipal BankingPrincipal principal) {
        return ApiResponse.ok(accountQueryService.listOwnedAccounts(principal.userId()));
    }

    @GetMapping("/{accountNumber}")
    public ApiResponse<AccountResponse> get(@AuthenticationPrincipal BankingPrincipal principal,
                                            @PathVariable String accountNumber) {
        return ApiResponse.ok(accountQueryService.getOwnedAccount(principal.userId(), accountNumber));
    }
}
