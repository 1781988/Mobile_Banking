package com.mobilebanking.platform.risk.repository;

import com.mobilebanking.platform.risk.domain.RiskEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskEventRepository extends JpaRepository<RiskEvent, Long> {
    Page<RiskEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
