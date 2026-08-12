package com.mobilebanking.platform.risk.service;

import com.mobilebanking.platform.risk.domain.RiskEvent;
import com.mobilebanking.platform.risk.domain.RiskEventType;
import com.mobilebanking.platform.risk.domain.RiskLevel;
import com.mobilebanking.platform.risk.repository.RiskEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;

@Service
@RequiredArgsConstructor
public class RiskEventService {

    private final RiskEventRepository riskEventRepository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId,
                       Long accountId,
                       String transferId,
                       RiskEventType eventType,
                       RiskLevel level,
                       BigDecimal amount,
                       String reason) {
        RiskEvent event = new RiskEvent();
        event.setUserId(userId);
        event.setAccountId(accountId);
        event.setTransferId(transferId);
        event.setEventType(eventType);
        event.setLevel(level);
        event.setAmount(amount);
        event.setReason(reason == null || reason.length() <= 255 ? reason : reason.substring(0, 255));
        event.setCreatedAt(clock.instant());
        riskEventRepository.save(event);
    }
}
