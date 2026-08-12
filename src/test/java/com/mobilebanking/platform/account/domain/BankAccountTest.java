package com.mobilebanking.platform.account.domain;

import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankAccountTest {

    private BankAccount account;

    @BeforeEach
    void setUp() {
        account = new BankAccount();
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("100.00"));
    }

    @Test
    void shouldDebitAndCreditWithExactScale() {
        Instant now = Instant.parse("2026-08-12T00:00:00Z");
        account.debit(new BigDecimal("30.00"), now);
        account.credit(new BigDecimal("5.50"), now);

        assertThat(account.getBalance()).isEqualByComparingTo("75.50");
        assertThat(account.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldRejectInsufficientBalance() {
        assertThatThrownBy(() -> account.debit(new BigDecimal("100.01"), Instant.now()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE));
    }

    @Test
    void shouldRejectFrozenAccount() {
        account.setStatus(AccountStatus.FROZEN);
        assertThatThrownBy(account::ensureActive)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_ACTIVE));
    }
}
