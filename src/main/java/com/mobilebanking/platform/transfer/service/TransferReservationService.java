package com.mobilebanking.platform.transfer.service;

import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.transfer.domain.TransferOrder;
import com.mobilebanking.platform.transfer.domain.TransferStatus;
import com.mobilebanking.platform.transfer.domain.TransferType;
import com.mobilebanking.platform.transfer.repository.TransferOrderRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

@Service
public class TransferReservationService {

    private final TransferOrderRepository transferOrderRepository;
    private final TransactionTemplate requiresNew;

    public TransferReservationService(TransferOrderRepository transferOrderRepository,
                                      PlatformTransactionManager transactionManager) {
        this.transferOrderRepository = transferOrderRepository;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public TransferReservationResult reserve(TransferReservationCommand command) {
        try {
            return Objects.requireNonNull(requiresNew.execute(status -> {
                TransferOrder existing = transferOrderRepository
                        .findByPayerAccountIdAndIdempotencyKey(command.payerAccountId(), command.idempotencyKey())
                        .orElse(null);
                if (existing != null) {
                    return new TransferReservationResult(existing, false);
                }

                TransferOrder order = new TransferOrder();
                order.setId(command.transferId());
                order.setPayerAccountId(command.payerAccountId());
                order.setPayeeAccountId(command.payeeAccountId());
                order.setPayerAccountNumber(command.payerAccountNumber());
                order.setPayeeAccountNumber(command.payeeAccountNumber());
                order.setInitiatedByUserId(command.initiatedByUserId());
                order.setIdempotencyKey(command.idempotencyKey());
                order.setRequestHash(command.requestHash());
                order.setAmount(command.amount());
                order.setCurrency(command.currency());
                order.setRemark(command.remark());
                order.setType(TransferType.INTERNAL_TRANSFER);
                order.setStatus(TransferStatus.PROCESSING);
                order.setCreatedAt(command.createdAt());
                transferOrderRepository.saveAndFlush(order);
                return new TransferReservationResult(order, true);
            }));
        } catch (DataIntegrityViolationException conflict) {
            return Objects.requireNonNull(requiresNew.execute(status -> {
                TransferOrder existing = transferOrderRepository
                        .findByPayerAccountIdAndIdempotencyKey(command.payerAccountId(), command.idempotencyKey())
                        .orElseThrow(() -> new BusinessException(ErrorCode.DATA_CONFLICT));
                return new TransferReservationResult(existing, false);
            }));
        }
    }
}
