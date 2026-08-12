package com.mobilebanking.platform.common.api;

import com.mobilebanking.platform.common.web.RequestIdFilter;
import org.slf4j.MDC;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        String requestId,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", "success", data,
                MDC.get(RequestIdFilter.MDC_KEY), Instant.now());
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, "CREATED", "created", data,
                MDC.get(RequestIdFilter.MDC_KEY), Instant.now());
    }

    public static <T> ApiResponse<T> accepted(T data) {
        return new ApiResponse<>(true, "ACCEPTED", "processing", data,
                MDC.get(RequestIdFilter.MDC_KEY), Instant.now());
    }
}
