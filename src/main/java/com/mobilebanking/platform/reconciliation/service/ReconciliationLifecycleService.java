package com.mobilebanking.platform.reconciliation.service;

import com.mobilebanking.platform.reconciliation.domain.ReconciliationBatch;
import com.mobilebanking.platform.reconciliation.domain.ReconciliationStatus;
import com.mobilebanking.platform.reconciliation.repository.ReconciliationBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReconciliationLifecycleService {

    private final ReconciliationBatchRepository batchRepository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReconciliationBatch start(LocalDate businessDate, Long initiatedByUserId) {
        ReconciliationBatch batch = new ReconciliationBatch();
        batch.setId(UUID.randomUUID().toString());
        batch.setBusinessDate(businessDate);
        batch.setStatus(ReconciliationStatus.RUNNING);
        batch.setTotalAccounts(0);
        batch.setMismatchCount(0);
        batch.setInitiatedByUserId(initiatedByUserId);
        batch.setStartedAt(clock.instant());
        return batchRepository.save(batch);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(String batchId, String reason) {
        batchRepository.findByIdForUpdate(batchId)
                .ifPresent(batch -> batch.fail(reason, clock.instant()));
    }
}
