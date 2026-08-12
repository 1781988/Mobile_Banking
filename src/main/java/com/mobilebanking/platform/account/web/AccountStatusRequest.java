package com.mobilebanking.platform.account.web;

import com.mobilebanking.platform.account.domain.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccountStatusRequest(
        @NotNull AccountStatus status,
        @NotBlank @Size(max = 200) String reason
) {
}
