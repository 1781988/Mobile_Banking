package com.mobilebanking.platform.account.service;

import com.mobilebanking.platform.account.domain.AccountStatus;
import com.mobilebanking.platform.account.domain.BankAccount;
import com.mobilebanking.platform.account.repository.BankAccountRepository;
import com.mobilebanking.platform.account.web.AccountResponse;
import com.mobilebanking.platform.audit.domain.AuditResult;
import com.mobilebanking.platform.audit.service.AuditService;
import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class AccountAdminService {

    private final BankAccountRepository bankAccountRepository;
    private final AuditService auditService;
    private final Clock clock;

    @Transactional
    public AccountResponse changeStatus(Long adminUserId,
                                        String accountNumber,
                                        AccountStatus status,
                                        String reason,
                                        String clientIp) {
        if (status == AccountStatus.CLOSED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "演示系统不支持直接销户");
        }
        BankAccount account = bankAccountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        AccountStatus before = account.getStatus();
        account.setStatus(status);
        account.setUpdatedAt(clock.instant());
        auditService.record(adminUserId, "ACCOUNT_STATUS_CHANGE", "ACCOUNT", accountNumber,
                AuditResult.SUCCESS, clientIp,
                "before=" + before + ",after=" + status + ",reason=" + reason);
        return AccountResponse.from(account);
    }
}
