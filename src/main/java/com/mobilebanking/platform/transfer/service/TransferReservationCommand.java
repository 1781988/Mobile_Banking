package com.mobilebanking.platform.transfer.service;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferReservationCommand(
        String transferId,
        Long payerAccountId,
        Long payeeAccountId,
        String payerAccountNumber,
        String payeeAccountNumber,
        Long initiatedByUserId,
        String idempotencyKey,
        String requestHash,
        BigDecimal amount,
        String currency,
        String remark,
        Instant createdAt
) {
}
