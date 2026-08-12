package com.mobilebanking.platform.reconciliation.service;

import com.mobilebanking.platform.account.repository.BankAccountRepository;
import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.reconciliation.domain.ReconciliationBatch;
import com.mobilebanking.platform.reconciliation.domain.ReconciliationItem;
import com.mobilebanking.platform.reconciliation.domain.ReconciliationItemStatus;
import com.mobilebanking.platform.reconciliation.repository.ReconciliationBatchRepository;
import com.mobilebanking.platform.reconciliation.repository.ReconciliationItemRepository;
import com.mobilebanking.platform.transfer.domain.LedgerDirection;
import com.mobilebanking.platform.transfer.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReconciliationCalculationService {

    private final ReconciliationBatchRepository batchRepository;
    private final ReconciliationItemRepository itemRepository;
    private final BankAccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final Clock clock;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ReconciliationBatch calculate(String batchId) {
        ReconciliationBatch batch = batchRepository.findByIdForUpdate(batchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECONCILIATION_NOT_FOUND));
        Instant now = clock.instant();
        List<ReconciliationItem> items = new ArrayList<>();
        int mismatchCount = 0;

        var accounts = accountRepository.findAll();
        for (var account : accounts) {
            BigDecimal credits = valueOrZero(ledgerEntryRepository.sumAmountByAccountAndDirection(
                    account.getId(), LedgerDirection.CREDIT));
            BigDecimal debits = valueOrZero(ledgerEntryRepository.sumAmountByAccountAndDirection(
                    account.getId(), LedgerDirection.DEBIT));
            BigDecimal calculated = account.getOpeningBalance()
                    .add(credits)
                    .subtract(debits)
                    .setScale(2, RoundingMode.UNNECESSARY);
            BigDecimal difference = account.getBalance()
                    .subtract(calculated)
                    .setScale(2, RoundingMode.UNNECESSARY);
            ReconciliationItemStatus status = difference.compareTo(BigDecimal.ZERO) == 0
                    ? ReconciliationItemStatus.MATCHED
                    : ReconciliationItemStatus.DIFFERENCE;
            if (status == ReconciliationItemStatus.DIFFERENCE) {
                mismatchCount++;
            }

            ReconciliationItem item = new ReconciliationItem();
            item.setBatchId(batchId);
            item.setAccountId(account.getId());
            item.setAccountNumber(account.getAccountNumber());
            item.setPersistedBalance(account.getBalance());
            item.setCalculatedBalance(calculated);
            item.setDifference(difference);
            item.setStatus(status);
            item.setCreatedAt(now);
            items.add(item);
        }
        itemRepository.saveAll(items);
        batch.complete(accounts.size(), mismatchCount, now);
        return batch;
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value;
    }
}
