package com.mobilebanking.platform.transfer.web;

import com.mobilebanking.platform.common.util.AccountMasker;
import com.mobilebanking.platform.transfer.domain.LedgerDirection;
import com.mobilebanking.platform.transfer.domain.LedgerEntry;

import java.math.BigDecimal;
import java.time.Instant;

public record StatementEntryResponse(
        Long entryId,
        String transferId,
        LedgerDirection direction,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String counterpartyAccountNumber,
        String description,
        Instant createdAt
) {
    public static StatementEntryResponse from(LedgerEntry entry) {
        return new StatementEntryResponse(
                entry.getId(),
                entry.getTransferId(),
                entry.getDirection(),
                entry.getAmount(),
                entry.getBalanceAfter(),
                AccountMasker.mask(entry.getCounterpartyAccountNumber()),
                entry.getDescription(),
                entry.getCreatedAt()
        );
    }
}
