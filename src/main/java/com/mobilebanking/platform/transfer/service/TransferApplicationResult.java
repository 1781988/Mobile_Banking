package com.mobilebanking.platform.transfer.service;

import com.mobilebanking.platform.transfer.web.TransferResponse;

public record TransferApplicationResult(
        TransferResponse response,
        boolean created
) {
}
