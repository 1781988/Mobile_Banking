package com.mobilebanking.platform.reconciliation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reconciliation_batch", indexes = {
        @Index(name = "idx_reconciliation_business_date", columnList = "business_date, started_at"),
        @Index(name = "idx_reconciliation_status", columnList = "status, started_at")
})
public class ReconciliationBatch {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReconciliationStatus status;

    @Column(name = "total_accounts", nullable = false)
    private int totalAccounts;

    @Column(name = "mismatch_count", nullable = false)
    private int mismatchCount;

    @Column(name = "initiated_by_user_id")
    private Long initiatedByUserId;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public void complete(int total, int mismatches, Instant now) {
        totalAccounts = total;
        mismatchCount = mismatches;
        status = mismatches == 0
                ? ReconciliationStatus.COMPLETED
                : ReconciliationStatus.COMPLETED_WITH_DIFFERENCES;
        completedAt = now;
        failureReason = null;
    }

    public void fail(String reason, Instant now) {
        status = ReconciliationStatus.FAILED;
        failureReason = reason == null || reason.length() <= 255 ? reason : reason.substring(0, 255);
        completedAt = now;
    }
}
