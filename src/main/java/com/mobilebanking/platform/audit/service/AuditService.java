package com.mobilebanking.platform.audit.service;

import com.mobilebanking.platform.audit.domain.AuditLog;
import com.mobilebanking.platform.audit.domain.AuditResult;
import com.mobilebanking.platform.audit.repository.AuditLogRepository;
import com.mobilebanking.platform.common.web.RequestIdFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final Clock clock;

    @Transactional
    public void record(Long actorUserId,
                       String action,
                       String resourceType,
                       String resourceId,
                       AuditResult result,
                       String clientIp,
                       String details) {
        AuditLog log = new AuditLog();
        log.setActorUserId(actorUserId);
        log.setAction(trim(action, 64));
        log.setResourceType(trim(resourceType, 64));
        log.setResourceId(trim(resourceId, 128));
        log.setResult(result);
        log.setRequestId(trim(MDC.get(RequestIdFilter.MDC_KEY), 64));
        log.setClientIp(trim(clientIp, 64));
        log.setDetails(details);
        log.setCreatedAt(clock.instant());
        auditLogRepository.save(log);
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
