package com.mobilebanking.platform.audit.web;

import com.mobilebanking.platform.audit.service.AuditQueryService;
import com.mobilebanking.platform.common.api.ApiResponse;
import com.mobilebanking.platform.common.api.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AuditAdminController {

    private final AuditQueryService auditQueryService;

    @GetMapping
    public ApiResponse<PageResponse<AuditLogResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(PageResponse.from(auditQueryService.list(
                PageRequest.of(Math.max(page, 0), safeSize))));
    }
}
