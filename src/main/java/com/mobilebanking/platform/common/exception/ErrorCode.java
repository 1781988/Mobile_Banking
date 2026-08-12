package com.mobilebanking.platform.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "请求参数不合法"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "用户名或密码错误"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "请先登录"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "无权执行该操作"),
    TOO_MANY_LOGIN_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "登录失败次数过多，请稍后重试"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "用户不存在"),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "账户不存在"),
    ACCOUNT_NOT_OWNED(HttpStatus.FORBIDDEN, "账户不属于当前用户"),
    ACCOUNT_NOT_ACTIVE(HttpStatus.CONFLICT, "账户当前不可交易"),
    CURRENCY_MISMATCH(HttpStatus.BAD_REQUEST, "付款账户与收款账户币种不一致"),
    SELF_TRANSFER(HttpStatus.BAD_REQUEST, "不允许向同一账户转账"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "交易金额不合法"),
    INSUFFICIENT_BALANCE(HttpStatus.CONFLICT, "账户可用余额不足"),
    SINGLE_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "超过单笔转账限额"),
    DAILY_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "超过当日累计转账限额"),
    IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "缺少 Idempotency-Key 请求头"),
    IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "同一幂等键对应了不同请求"),
    TRANSFER_NOT_FOUND(HttpStatus.NOT_FOUND, "转账订单不存在"),
    TRANSFER_PROCESSING(HttpStatus.CONFLICT, "转账正在处理中"),
    RECONCILIATION_NOT_FOUND(HttpStatus.NOT_FOUND, "对账批次不存在"),
    DATA_CONFLICT(HttpStatus.CONFLICT, "数据状态冲突，请刷新后重试"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "系统处理失败"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "依赖服务暂不可用");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    public static ErrorCode fromName(String value, ErrorCode fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return ErrorCode.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
