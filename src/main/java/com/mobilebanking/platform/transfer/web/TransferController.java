package com.mobilebanking.platform.transfer.web;

import com.mobilebanking.platform.auth.security.BankingPrincipal;
import com.mobilebanking.platform.common.api.ApiResponse;
import com.mobilebanking.platform.common.api.PageResponse;
import com.mobilebanking.platform.common.web.ClientIpResolver;
import com.mobilebanking.platform.transfer.domain.TransferStatus;
import com.mobilebanking.platform.transfer.service.TransferApplicationResult;
import com.mobilebanking.platform.transfer.service.TransferApplicationService;
import com.mobilebanking.platform.transfer.service.TransferQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferApplicationService transferApplicationService;
    private final TransferQueryService transferQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponse>> create(
            @AuthenticationPrincipal BankingPrincipal principal,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest request,
            HttpServletRequest httpRequest) {
        TransferApplicationResult result = transferApplicationService.create(
                principal,
                idempotencyKey,
                request,
                ClientIpResolver.resolve(httpRequest));

        HttpStatus status;
        ApiResponse<TransferResponse> body;
        if (result.response().status() == TransferStatus.PROCESSING) {
            status = HttpStatus.ACCEPTED;
            body = ApiResponse.accepted(result.response());
        } else if (result.created()) {
            status = HttpStatus.CREATED;
            body = ApiResponse.created(result.response());
        } else {
            status = HttpStatus.OK;
            body = ApiResponse.ok(result.response());
        }
        return ResponseEntity.status(status).body(body);
    }

    @GetMapping("/{transferId}")
    public ApiResponse<TransferResponse> get(
            @AuthenticationPrincipal BankingPrincipal principal,
            @PathVariable String transferId) {
        return ApiResponse.ok(transferQueryService.getVisible(principal, transferId));
    }

    @GetMapping
    public ApiResponse<PageResponse<TransferResponse>> list(
            @AuthenticationPrincipal BankingPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(PageResponse.from(transferQueryService.listVisible(
                principal,
                PageRequest.of(Math.max(page, 0), safeSize))));
    }
}
