package com.mobilebanking.platform.transfer.service;

import com.mobilebanking.platform.account.repository.BankAccountRepository;
import com.mobilebanking.platform.auth.security.BankingPrincipal;
import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.transfer.domain.TransferOrder;
import com.mobilebanking.platform.transfer.repository.TransferOrderRepository;
import com.mobilebanking.platform.transfer.web.TransferResponse;
import com.mobilebanking.platform.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferQueryService {

    private final TransferOrderRepository transferOrderRepository;
    private final BankAccountRepository bankAccountRepository;

    @Transactional(readOnly = true)
    public TransferResponse getVisible(BankingPrincipal principal, String transferId) {
        TransferOrder order = transferOrderRepository.findById(transferId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSFER_NOT_FOUND));
        if (principal.role() != UserRole.ADMIN && !isVisibleToUser(principal.userId(), order)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return TransferResponse.from(order, false);
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> listVisible(BankingPrincipal principal, Pageable pageable) {
        if (principal.role() == UserRole.ADMIN) {
            return transferOrderRepository.findAllByOrderByCreatedAtDesc(pageable)
                    .map(order -> TransferResponse.from(order, false));
        }
        List<Long> accountIds = bankAccountRepository.findAllByUserIdOrderByCreatedAtAsc(principal.userId())
                .stream()
                .map(account -> account.getId())
                .toList();
        Page<TransferOrder> page = accountIds.isEmpty()
                ? transferOrderRepository.findAllByInitiatedByUserIdOrderByCreatedAtDesc(principal.userId(), pageable)
                : transferOrderRepository.findVisibleToUser(principal.userId(), accountIds, pageable);
        return page.map(order -> TransferResponse.from(order, false));
    }

    private boolean isVisibleToUser(Long userId, TransferOrder order) {
        if (order.getInitiatedByUserId().equals(userId)) {
            return true;
        }
        return bankAccountRepository.findById(order.getPayeeAccountId())
                .map(account -> account.getUser().getId().equals(userId))
                .orElse(false);
    }
}
