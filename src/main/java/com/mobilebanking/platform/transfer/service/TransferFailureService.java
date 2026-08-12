package com.mobilebanking.platform.transfer.service;

import com.mobilebanking.platform.audit.domain.AuditResult;
import com.mobilebanking.platform.audit.service.AuditService;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.transfer.domain.TransferOrder;
import com.mobilebanking.platform.transfer.domain.TransferStatus;
import com.mobilebanking.platform.transfer.repository.TransferOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class TransferFailureService {

    private final TransferOrderRepository transferOrderRepository;
    private final AuditService auditService;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String transferId, ErrorCode code, String message, String clientIp) {
        TransferOrder order = transferOrderRepository.findByIdForUpdate(transferId).orElse(null);
        if (order == null || order.getStatus() != TransferStatus.PROCESSING) {
            return;
        }
        order.markFailed(code.name(), message, clock.instant());
        auditService.record(order.getInitiatedByUserId(), "TRANSFER_EXECUTE", "TRANSFER", transferId,
                AuditResult.FAILURE, clientIp, "code=" + code.name() + ",message=" + message);
    }
}
