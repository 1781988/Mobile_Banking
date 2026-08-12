package com.mobilebanking.platform.risk.service;

import com.mobilebanking.platform.risk.repository.RiskEventRepository;
import com.mobilebanking.platform.risk.web.RiskEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiskEventQueryService {

    private final RiskEventRepository riskEventRepository;

    @Transactional(readOnly = true)
    public Page<RiskEventResponse> list(Pageable pageable) {
        return riskEventRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(RiskEventResponse::from);
    }
}
