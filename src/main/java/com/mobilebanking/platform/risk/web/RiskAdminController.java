package com.mobilebanking.platform.risk.web;

import com.mobilebanking.platform.common.api.ApiResponse;
import com.mobilebanking.platform.common.api.PageResponse;
import com.mobilebanking.platform.risk.service.RiskEventQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/risk-events")
@RequiredArgsConstructor
public class RiskAdminController {

    private final RiskEventQueryService riskEventQueryService;

    @GetMapping
    public ApiResponse<PageResponse<RiskEventResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(PageResponse.from(riskEventQueryService.list(
                PageRequest.of(Math.max(page, 0), safeSize))));
    }
}
