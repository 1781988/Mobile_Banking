package com.mobilebanking.platform.transfer.service;

import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.config.BankProperties;
import com.mobilebanking.platform.transfer.domain.TransferStatus;
import com.mobilebanking.platform.transfer.repository.TransferOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferRecoveryService {

    private final TransferOrderRepository transferOrderRepository;
    private final TransferFailureService failureService;
    private final BankProperties bankProperties;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${bank.transfer.recovery-interval:PT1M}")
    public void failStaleProcessingOrders() {
        var cutoff = clock.instant().minus(bankProperties.getTransfer().getProcessingTimeout());
        var orders = transferOrderRepository
                .findTop100ByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(TransferStatus.PROCESSING, cutoff);
        for (var order : orders) {
            try {
                failureService.markFailed(order.getId(), ErrorCode.INTERNAL_ERROR,
                        "交易处理超时，未发生资金变动，请使用新的幂等键重试", "system");
            } catch (RuntimeException exception) {
                log.error("Failed to recover stale transfer {}", order.getId(), exception);
            }
        }
    }
}
