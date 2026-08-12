package com.mobilebanking.platform.common.util;

import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyUtilsTest {

    @Test
    void shouldNormalizeValidMoney() {
        assertThat(MoneyUtils.normalizePositive(new BigDecimal("12.30")))
                .isEqualByComparingTo("12.30");
    }

    @Test
    void shouldRejectMoreThanTwoFractionDigits() {
        assertThatThrownBy(() -> MoneyUtils.normalizePositive(new BigDecimal("1.001")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_AMOUNT));
    }

    @Test
    void shouldRejectZeroAndNegativeValues() {
        assertThatThrownBy(() -> MoneyUtils.normalizePositive(BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> MoneyUtils.normalizePositive(new BigDecimal("-1.00")))
                .isInstanceOf(BusinessException.class);
    }
}
