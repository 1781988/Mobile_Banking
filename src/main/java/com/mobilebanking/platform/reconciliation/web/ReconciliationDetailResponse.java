package com.mobilebanking.platform.reconciliation.web;

import java.util.List;

public record ReconciliationDetailResponse(
        ReconciliationBatchResponse batch,
        List<ReconciliationItemResponse> items
) {
}
