package com.mobilebanking.platform.transfer.web;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateTransferRequest(
        @NotBlank
        @Pattern(regexp = "\\d{10,32}", message = "付款账号格式不合法")
        String payerAccountNumber,

        @NotBlank
        @Pattern(regexp = "\\d{10,32}", message = "收款账号格式不合法")
        String payeeAccountNumber,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false, message = "金额必须大于 0")
        @Digits(integer = 17, fraction = 2, message = "金额最多 17 位整数和 2 位小数")
        BigDecimal amount,

        @Size(max = 140, message = "备注不能超过 140 个字符")
        String remark
) {
}
