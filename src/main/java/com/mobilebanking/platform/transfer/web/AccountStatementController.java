package com.mobilebanking.platform.transfer.web;

import com.mobilebanking.platform.auth.security.BankingPrincipal;
import com.mobilebanking.platform.common.api.ApiResponse;
import com.mobilebanking.platform.common.api.PageResponse;
import com.mobilebanking.platform.transfer.service.AccountStatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts/{accountNumber}/statement")
@RequiredArgsConstructor
public class AccountStatementController {

    private final AccountStatementService accountStatementService;

    @GetMapping
    public ApiResponse<PageResponse<StatementEntryResponse>> statement(
            @AuthenticationPrincipal BankingPrincipal principal,
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(PageResponse.from(accountStatementService.getStatement(
                principal.userId(),
                accountNumber,
                PageRequest.of(Math.max(page, 0), safeSize))));
    }
}
