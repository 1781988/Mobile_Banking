package com.mobilebanking.platform.common.api;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        boolean success,
        String code,
        String message,
        String requestId,
        String path,
        Map<String, String> fieldErrors,
        Instant timestamp
) {
}
