package com.mobilebanking.platform.transfer.web;

import com.mobilebanking.platform.common.util.AccountMasker;
import com.mobilebanking.platform.transfer.domain.TransferOrder;
import com.mobilebanking.platform.transfer.domain.TransferStatus;
import com.mobilebanking.platform.transfer.domain.TransferType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        String transferId,
        TransferType type,
        TransferStatus status,
        String payerAccountNumber,
        String payeeAccountNumber,
        BigDecimal amount,
        String currency,
        String remark,
        String errorCode,
        String errorMessage,
        Instant createdAt,
        Instant completedAt,
        boolean replayed
) {
    public static TransferResponse from(TransferOrder order, boolean replayed) {
        return new TransferResponse(
                order.getId(),
                order.getType(),
                order.getStatus(),
                AccountMasker.mask(order.getPayerAccountNumber()),
                AccountMasker.mask(order.getPayeeAccountNumber()),
                order.getAmount(),
                order.getCurrency(),
                order.getRemark(),
                order.getErrorCode(),
                order.getErrorMessage(),
                order.getCreatedAt(),
                order.getCompletedAt(),
                replayed
        );
    }
}
