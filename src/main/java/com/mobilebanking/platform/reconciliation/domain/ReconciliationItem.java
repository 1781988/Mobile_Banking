package com.mobilebanking.platform.reconciliation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reconciliation_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reconciliation_batch_account",
                columnNames = {"batch_id", "account_id"}),
        indexes = @Index(name = "idx_reconciliation_item_status", columnList = "batch_id, status"))
public class ReconciliationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false, length = 36)
    private String batchId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "account_number", nullable = false, length = 32)
    private String accountNumber;

    @Column(name = "persisted_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal persistedBalance;

    @Column(name = "calculated_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal calculatedBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal difference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReconciliationItemStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
