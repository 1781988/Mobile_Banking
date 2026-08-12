package com.mobilebanking.platform.transfer.service;

import com.mobilebanking.platform.account.domain.BankAccount;
import com.mobilebanking.platform.account.repository.BankAccountRepository;
import com.mobilebanking.platform.audit.domain.AuditResult;
import com.mobilebanking.platform.audit.service.AuditService;
import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.common.util.AccountMasker;
import com.mobilebanking.platform.transfer.domain.LedgerDirection;
import com.mobilebanking.platform.transfer.domain.LedgerEntry;
import com.mobilebanking.platform.transfer.domain.TransferOrder;
import com.mobilebanking.platform.transfer.domain.TransferStatus;
import com.mobilebanking.platform.transfer.repository.LedgerEntryRepository;
import com.mobilebanking.platform.transfer.repository.TransferOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferExecutionService {

    private final TransferOrderRepository transferOrderRepository;
    private final BankAccountRepository bankAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final RiskPolicyService riskPolicyService;
    private final AuditService auditService;
    private final Clock clock;

    @Transactional
    public TransferOrder execute(String transferId, String clientIp) {
        TransferOrder order = transferOrderRepository.findByIdForUpdate(transferId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSFER_NOT_FOUND));
        if (order.getStatus() != TransferStatus.PROCESSING) {
            return order;
        }

        List<BankAccount> lockedAccounts = bankAccountRepository.findAllByAccountNumberInForUpdate(
                List.of(order.getPayerAccountNumber(), order.getPayeeAccountNumber()));
        if (lockedAccounts.size() != 2) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        Map<String, BankAccount> accounts = lockedAccounts.stream()
                .collect(Collectors.toMap(BankAccount::getAccountNumber, Function.identity()));
        BankAccount payer = accounts.get(order.getPayerAccountNumber());
        BankAccount payee = accounts.get(order.getPayeeAccountNumber());
        if (payer == null || payee == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (!payer.getUser().getId().equals(order.getInitiatedByUserId())) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_OWNED);
        }
        if (payer.getId().equals(payee.getId())) {
            throw new BusinessException(ErrorCode.SELF_TRANSFER);
        }

        payer.ensureActive();
        payee.ensureActive();
        if (!payer.getCurrency().equals(payee.getCurrency())
                || !payer.getCurrency().equals(order.getCurrency())) {
            throw new BusinessException(ErrorCode.CURRENCY_MISMATCH);
        }

        riskPolicyService.validate(payer.getId(), order.getAmount());
        Instant now = clock.instant();
        payer.debit(order.getAmount(), now);
        payee.credit(order.getAmount(), now);

        LedgerEntry debit = buildEntry(order, payer, payee, LedgerDirection.DEBIT, now);
        LedgerEntry credit = buildEntry(order, payee, payer, LedgerDirection.CREDIT, now);
        ledgerEntryRepository.saveAll(List.of(debit, credit));
        order.markSucceeded(now);

        auditService.record(order.getInitiatedByUserId(), "TRANSFER_EXECUTE", "TRANSFER", order.getId(),
                AuditResult.SUCCESS, clientIp,
                "payer=" + AccountMasker.mask(order.getPayerAccountNumber())
                        + ",payee=" + AccountMasker.mask(order.getPayeeAccountNumber())
                        + ",amount=" + order.getAmount().toPlainString() + ",currency=" + order.getCurrency());
        return order;
    }

    private LedgerEntry buildEntry(TransferOrder order,
                                   BankAccount account,
                                   BankAccount counterparty,
                                   LedgerDirection direction,
                                   Instant now) {
        LedgerEntry entry = new LedgerEntry();
        entry.setTransferId(order.getId());
        entry.setAccountId(account.getId());
        entry.setAccountNumber(account.getAccountNumber());
        entry.setDirection(direction);
        entry.setAmount(order.getAmount());
        entry.setBalanceAfter(account.getBalance());
        entry.setCounterpartyAccountNumber(counterparty.getAccountNumber());
        entry.setDescription(order.getRemark());
        entry.setCreatedAt(now);
        return entry;
    }
}
