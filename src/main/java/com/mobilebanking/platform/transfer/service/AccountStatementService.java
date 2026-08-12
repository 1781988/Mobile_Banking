package com.mobilebanking.platform.transfer.service;

import com.mobilebanking.platform.account.domain.BankAccount;
import com.mobilebanking.platform.account.repository.BankAccountRepository;
import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.transfer.repository.LedgerEntryRepository;
import com.mobilebanking.platform.transfer.web.StatementEntryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountStatementService {

    private final BankAccountRepository bankAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional(readOnly = true)
    public Page<StatementEntryResponse> getStatement(Long userId,
                                                     String accountNumber,
                                                     Pageable pageable) {
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (!account.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_OWNED);
        }
        return ledgerEntryRepository.findAllByAccountIdOrderByCreatedAtDesc(account.getId(), pageable)
                .map(StatementEntryResponse::from);
    }
}
