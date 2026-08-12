package com.mobilebanking.platform.account.web;

import com.mobilebanking.platform.account.domain.AccountStatus;
import com.mobilebanking.platform.account.domain.BankAccount;
import com.mobilebanking.platform.common.util.AccountMasker;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        Long id,
        String accountNumber,
        String maskedAccountNumber,
        String ownerName,
        String currency,
        BigDecimal balance,
        AccountStatus status,
        Instant createdAt
) {
    public static AccountResponse from(BankAccount account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                AccountMasker.mask(account.getAccountNumber()),
                account.getUser().getDisplayName(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }
}
