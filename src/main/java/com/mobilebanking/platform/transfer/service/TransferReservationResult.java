package com.mobilebanking.platform.transfer.service;

import com.mobilebanking.platform.transfer.domain.TransferOrder;

public record TransferReservationResult(TransferOrder order, boolean created) {
}
