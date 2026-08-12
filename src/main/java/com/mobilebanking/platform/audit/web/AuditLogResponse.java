package com.mobilebanking.platform.audit.web;

import com.mobilebanking.platform.audit.domain.AuditLog;
import com.mobilebanking.platform.audit.domain.AuditResult;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String action,
        String resourceType,
        String resourceId,
        AuditResult result,
        String requestId,
        String clientIp,
        String details,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorUserId(),
                log.getAction(),
                log.getResourceType(),
                log.getResourceId(),
                log.getResult(),
                log.getRequestId(),
                log.getClientIp(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}
