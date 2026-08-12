package com.mobilebanking.platform.reconciliation.repository;

import com.mobilebanking.platform.reconciliation.domain.ReconciliationBatch;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReconciliationBatchRepository extends JpaRepository<ReconciliationBatch, String> {

    Page<ReconciliationBatch> findAllByOrderByStartedAtDesc(Pageable pageable);

    Optional<ReconciliationBatch> findTopByOrderByStartedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from ReconciliationBatch b where b.id = :id")
    Optional<ReconciliationBatch> findByIdForUpdate(@Param("id") String id);
}
