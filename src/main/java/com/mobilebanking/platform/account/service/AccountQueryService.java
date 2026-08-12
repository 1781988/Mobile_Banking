package com.mobilebanking.platform.account.service;

import com.mobilebanking.platform.account.domain.BankAccount;
import com.mobilebanking.platform.account.repository.BankAccountRepository;
import com.mobilebanking.platform.account.web.AccountResponse;
import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountQueryService {

    private final BankAccountRepository bankAccountRepository;

    @Transactional(readOnly = true)
    public List<AccountResponse> listOwnedAccounts(Long userId) {
        return bankAccountRepository.findAllByUserIdOrderByCreatedAtAsc(userId)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getOwnedAccount(Long userId, String accountNumber) {
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (!account.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_OWNED);
        }
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> listAll(Pageable pageable) {
        return bankAccountRepository.findAllByOrderByIdAsc(pageable).map(AccountResponse::from);
    }
}
