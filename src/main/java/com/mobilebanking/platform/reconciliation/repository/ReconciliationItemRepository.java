package com.mobilebanking.platform.reconciliation.repository;

import com.mobilebanking.platform.reconciliation.domain.ReconciliationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReconciliationItemRepository extends JpaRepository<ReconciliationItem, Long> {
    List<ReconciliationItem> findAllByBatchIdOrderByAccountIdAsc(String batchId);
}
