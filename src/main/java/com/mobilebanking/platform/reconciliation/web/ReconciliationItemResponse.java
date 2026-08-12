package com.mobilebanking.platform.reconciliation.web;

import com.mobilebanking.platform.common.util.AccountMasker;
import com.mobilebanking.platform.reconciliation.domain.ReconciliationItem;
import com.mobilebanking.platform.reconciliation.domain.ReconciliationItemStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ReconciliationItemResponse(
        Long id,
        Long accountId,
        String accountNumber,
        BigDecimal persistedBalance,
        BigDecimal calculatedBalance,
        BigDecimal difference,
        ReconciliationItemStatus status,
        Instant createdAt
) {
    public static ReconciliationItemResponse from(ReconciliationItem item) {
        return new ReconciliationItemResponse(
                item.getId(),
                item.getAccountId(),
                AccountMasker.mask(item.getAccountNumber()),
                item.getPersistedBalance(),
                item.getCalculatedBalance(),
                item.getDifference(),
                item.getStatus(),
                item.getCreatedAt()
        );
    }
}
