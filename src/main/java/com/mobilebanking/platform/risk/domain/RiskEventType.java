package com.mobilebanking.platform.risk.domain;

public enum RiskEventType {
    SINGLE_LIMIT_EXCEEDED,
    DAILY_LIMIT_EXCEEDED,
    INSUFFICIENT_BALANCE,
    ACCOUNT_RESTRICTED,
    TRANSFER_SYSTEM_ERROR
}
