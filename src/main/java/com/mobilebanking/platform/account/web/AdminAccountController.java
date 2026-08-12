package com.mobilebanking.platform.account.web;

import com.mobilebanking.platform.account.service.AccountAdminService;
import com.mobilebanking.platform.account.service.AccountQueryService;
import com.mobilebanking.platform.auth.security.BankingPrincipal;
import com.mobilebanking.platform.common.api.ApiResponse;
import com.mobilebanking.platform.common.api.PageResponse;
import com.mobilebanking.platform.common.web.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AccountQueryService accountQueryService;
    private final AccountAdminService accountAdminService;

    @GetMapping
    public ApiResponse<PageResponse<AccountResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(PageResponse.from(accountQueryService.listAll(
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by("id").ascending()))));
    }

    @PutMapping("/{accountNumber}/status")
    public ApiResponse<AccountResponse> changeStatus(
            @AuthenticationPrincipal BankingPrincipal principal,
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountStatusRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(accountAdminService.changeStatus(
                principal.userId(),
                accountNumber,
                request.status(),
                request.reason(),
                ClientIpResolver.resolve(httpRequest)
        ));
    }
}
