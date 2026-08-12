package com.mobilebanking.platform.reconciliation.web;

import com.mobilebanking.platform.auth.security.BankingPrincipal;
import com.mobilebanking.platform.common.api.ApiResponse;
import com.mobilebanking.platform.common.api.PageResponse;
import com.mobilebanking.platform.common.web.ClientIpResolver;
import com.mobilebanking.platform.reconciliation.service.ReconciliationApplicationService;
import com.mobilebanking.platform.reconciliation.service.ReconciliationQueryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reconciliations")
@RequiredArgsConstructor
public class ReconciliationAdminController {

    private final ReconciliationApplicationService applicationService;
    private final ReconciliationQueryService queryService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReconciliationBatchResponse>> run(
            @AuthenticationPrincipal BankingPrincipal principal,
            HttpServletRequest request) {
        var result = applicationService.run(principal.userId(), ClientIpResolver.resolve(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    @GetMapping
    public ApiResponse<PageResponse<ReconciliationBatchResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(PageResponse.from(queryService.list(
                PageRequest.of(Math.max(page, 0), safeSize))));
    }

    @GetMapping("/latest")
    public ApiResponse<ReconciliationBatchResponse> latest() {
        return ApiResponse.ok(queryService.latest());
    }

    @GetMapping("/{batchId}")
    public ApiResponse<ReconciliationDetailResponse> get(@PathVariable String batchId) {
        return ApiResponse.ok(queryService.get(batchId));
    }
}
