package com.mobilebanking.platform.risk.web;

import com.mobilebanking.platform.risk.domain.RiskEvent;
import com.mobilebanking.platform.risk.domain.RiskEventType;
import com.mobilebanking.platform.risk.domain.RiskLevel;

import java.math.BigDecimal;
import java.time.Instant;

public record RiskEventResponse(
        Long id,
        Long userId,
        Long accountId,
        String transferId,
        RiskEventType eventType,
        RiskLevel level,
        BigDecimal amount,
        String reason,
        Instant createdAt
) {
    public static RiskEventResponse from(RiskEvent event) {
        return new RiskEventResponse(
                event.getId(),
                event.getUserId(),
                event.getAccountId(),
                event.getTransferId(),
                event.getEventType(),
                event.getLevel(),
                event.getAmount(),
                event.getReason(),
                event.getCreatedAt()
        );
    }
}
