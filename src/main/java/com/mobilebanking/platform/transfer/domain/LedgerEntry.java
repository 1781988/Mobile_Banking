package com.mobilebanking.platform.transfer.domain;

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
@Table(name = "ledger_entry", indexes = {
        @Index(name = "idx_ledger_account_created", columnList = "account_id, created_at"),
        @Index(name = "idx_ledger_transfer", columnList = "transfer_id")
})
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_id", nullable = false, length = 36)
    private String transferId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "account_number", nullable = false, length = 32)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LedgerDirection direction;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "counterparty_account_number", nullable = false, length = 32)
    private String counterpartyAccountNumber;

    @Column(length = 140)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
