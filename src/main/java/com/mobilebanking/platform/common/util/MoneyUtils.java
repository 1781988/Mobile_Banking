package com.mobilebanking.platform.common.util;

import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    public static final int SCALE = 2;
    public static final BigDecimal ZERO = new BigDecimal("0.00");

    private MoneyUtils() {
    }

    public static BigDecimal normalizePositive(BigDecimal value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT);
        }
        try {
            BigDecimal normalized = value.setScale(SCALE, RoundingMode.UNNECESSARY);
            if (normalized.compareTo(ZERO) <= 0) {
                throw new BusinessException(ErrorCode.INVALID_AMOUNT);
            }
            return normalized;
        } catch (ArithmeticException exception) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "金额最多保留两位小数");
        }
    }

    public static BigDecimal normalize(BigDecimal value) {
        return value == null ? ZERO : value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
