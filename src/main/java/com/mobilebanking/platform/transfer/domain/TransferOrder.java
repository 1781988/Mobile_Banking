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
@Table(name = "transfer_order",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_transfer_payer_idempotency",
                columnNames = {"payer_account_id", "idempotency_key"}),
        indexes = {
                @Index(name = "idx_transfer_initiator_created", columnList = "initiated_by_user_id, created_at"),
                @Index(name = "idx_transfer_payee_created", columnList = "payee_account_id, created_at"),
                @Index(name = "idx_transfer_status_created", columnList = "status, created_at")
        })
public class TransferOrder {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "payer_account_id", nullable = false)
    private Long payerAccountId;

    @Column(name = "payee_account_id", nullable = false)
    private Long payeeAccountId;

    @Column(name = "payer_account_number", nullable = false, length = 32)
    private String payerAccountNumber;

    @Column(name = "payee_account_number", nullable = false, length = 32)
    private String payeeAccountNumber;

    @Column(name = "initiated_by_user_id", nullable = false)
    private Long initiatedByUserId;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 140)
    private String remark;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransferType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransferStatus status;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public void markSucceeded(Instant now) {
        status = TransferStatus.SUCCEEDED;
        errorCode = null;
        errorMessage = null;
        completedAt = now;
    }

    public void markFailed(String code, String message, Instant now) {
        status = TransferStatus.FAILED;
        errorCode = code;
        errorMessage = message == null || message.length() <= 255 ? message : message.substring(0, 255);
        completedAt = now;
    }
}
