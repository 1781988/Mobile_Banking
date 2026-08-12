package com.mobilebanking.platform.reconciliation.web;

import com.mobilebanking.platform.reconciliation.domain.ReconciliationBatch;
import com.mobilebanking.platform.reconciliation.domain.ReconciliationStatus;

import java.time.Instant;
import java.time.LocalDate;

public record ReconciliationBatchResponse(
        String id,
        LocalDate businessDate,
        ReconciliationStatus status,
        int totalAccounts,
        int mismatchCount,
        Long initiatedByUserId,
        String failureReason,
        Instant startedAt,
        Instant completedAt
) {
    public static ReconciliationBatchResponse from(ReconciliationBatch batch) {
        return new ReconciliationBatchResponse(
                batch.getId(),
                batch.getBusinessDate(),
                batch.getStatus(),
                batch.getTotalAccounts(),
                batch.getMismatchCount(),
                batch.getInitiatedByUserId(),
                batch.getFailureReason(),
                batch.getStartedAt(),
                batch.getCompletedAt()
        );
    }
}
