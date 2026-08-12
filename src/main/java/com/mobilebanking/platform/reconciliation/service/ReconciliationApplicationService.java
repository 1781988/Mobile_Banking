package com.mobilebanking.platform.reconciliation.service;

import com.mobilebanking.platform.audit.domain.AuditResult;
import com.mobilebanking.platform.audit.service.AuditService;
import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.config.BankProperties;
import com.mobilebanking.platform.reconciliation.domain.ReconciliationBatch;
import com.mobilebanking.platform.reconciliation.web.ReconciliationBatchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ReconciliationApplicationService {

    private final ReconciliationLifecycleService lifecycleService;
    private final ReconciliationCalculationService calculationService;
    private final AuditService auditService;
    private final BankProperties bankProperties;
    private final Clock clock;

    public ReconciliationBatchResponse run(Long actorUserId, String clientIp) {
        ZoneId zone = ZoneId.of(bankProperties.getTransfer().getZoneId());
        var businessDate = clock.instant().atZone(zone).toLocalDate();
        ReconciliationBatch batch = lifecycleService.start(businessDate, actorUserId);
        try {
            ReconciliationBatch completed = calculationService.calculate(batch.getId());
            auditService.record(actorUserId, "RECONCILIATION_RUN", "RECONCILIATION", completed.getId(),
                    AuditResult.SUCCESS, clientIp,
                    "businessDate=" + businessDate + ",mismatchCount=" + completed.getMismatchCount());
            return ReconciliationBatchResponse.from(completed);
        } catch (RuntimeException exception) {
            lifecycleService.fail(batch.getId(), exception.getMessage());
            auditService.record(actorUserId, "RECONCILIATION_RUN", "RECONCILIATION", batch.getId(),
                    AuditResult.FAILURE, clientIp, exception.getClass().getSimpleName());
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "对账执行失败");
        }
    }
}
