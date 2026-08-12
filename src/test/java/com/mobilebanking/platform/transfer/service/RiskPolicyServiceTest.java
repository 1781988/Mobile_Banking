package com.mobilebanking.platform.transfer.service;

import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.config.BankProperties;
import com.mobilebanking.platform.transfer.domain.TransferStatus;
import com.mobilebanking.platform.transfer.repository.TransferOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RiskPolicyServiceTest {

    private TransferOrderRepository repository;
    private RiskPolicyService service;

    @BeforeEach
    void setUp() {
        repository = mock(TransferOrderRepository.class);
        BankProperties properties = new BankProperties();
        properties.getTransfer().setSingleLimit(new BigDecimal("500.00"));
        properties.getTransfer().setDailyLimit(new BigDecimal("1000.00"));
        Clock clock = Clock.fixed(Instant.parse("2026-08-12T04:00:00Z"), ZoneOffset.UTC);
        service = new RiskPolicyService(repository, properties, clock);
    }

    @Test
    void shouldAllowAmountWithinLimits() {
        when(repository.sumAmountByPayerAndStatusSince(eq(1L), eq(TransferStatus.SUCCEEDED), any()))
                .thenReturn(new BigDecimal("400.00"));
        assertThatCode(() -> service.validate(1L, new BigDecimal("500.00")))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectSingleLimit() {
        assertThatThrownBy(() -> service.validate(1L, new BigDecimal("500.01")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SINGLE_LIMIT_EXCEEDED));
    }

    @Test
    void shouldRejectDailyLimit() {
        when(repository.sumAmountByPayerAndStatusSince(eq(1L), eq(TransferStatus.SUCCEEDED), any()))
                .thenReturn(new BigDecimal("800.00"));
        assertThatThrownBy(() -> service.validate(1L, new BigDecimal("300.00")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DAILY_LIMIT_EXCEEDED));
    }
}
