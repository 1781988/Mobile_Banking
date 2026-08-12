package com.mobilebanking.platform.transfer.service;

import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.config.BankProperties;
import com.mobilebanking.platform.transfer.domain.TransferStatus;
import com.mobilebanking.platform.transfer.repository.TransferOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class RiskPolicyService {

    private final TransferOrderRepository transferOrderRepository;
    private final BankProperties bankProperties;
    private final Clock clock;

    public void validate(Long payerAccountId, BigDecimal amount) {
        if (amount.compareTo(bankProperties.getTransfer().getSingleLimit()) > 0) {
            throw new BusinessException(ErrorCode.SINGLE_LIMIT_EXCEEDED);
        }

        ZoneId zoneId = ZoneId.of(bankProperties.getTransfer().getZoneId());
        ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), zoneId);
        Instant startOfDay = now.toLocalDate().atStartOfDay(zoneId).toInstant();
        BigDecimal transferredToday = transferOrderRepository.sumAmountByPayerAndStatusSince(
                payerAccountId, TransferStatus.SUCCEEDED, startOfDay);
        if (transferredToday == null) {
            transferredToday = BigDecimal.ZERO;
        }
        if (transferredToday.add(amount).compareTo(bankProperties.getTransfer().getDailyLimit()) > 0) {
            throw new BusinessException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }
    }
}
