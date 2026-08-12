package com.mobilebanking.platform.reconciliation.service;

import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.reconciliation.repository.ReconciliationBatchRepository;
import com.mobilebanking.platform.reconciliation.repository.ReconciliationItemRepository;
import com.mobilebanking.platform.reconciliation.web.ReconciliationBatchResponse;
import com.mobilebanking.platform.reconciliation.web.ReconciliationDetailResponse;
import com.mobilebanking.platform.reconciliation.web.ReconciliationItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReconciliationQueryService {

    private final ReconciliationBatchRepository batchRepository;
    private final ReconciliationItemRepository itemRepository;

    @Transactional(readOnly = true)
    public Page<ReconciliationBatchResponse> list(Pageable pageable) {
        return batchRepository.findAllByOrderByStartedAtDesc(pageable)
                .map(ReconciliationBatchResponse::from);
    }

    @Transactional(readOnly = true)
    public ReconciliationDetailResponse get(String batchId) {
        var batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECONCILIATION_NOT_FOUND));
        var items = itemRepository.findAllByBatchIdOrderByAccountIdAsc(batchId)
                .stream()
                .map(ReconciliationItemResponse::from)
                .toList();
        return new ReconciliationDetailResponse(ReconciliationBatchResponse.from(batch), items);
    }

    @Transactional(readOnly = true)
    public ReconciliationBatchResponse latest() {
        return batchRepository.findTopByOrderByStartedAtDesc()
                .map(ReconciliationBatchResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECONCILIATION_NOT_FOUND));
    }
}
