package com.mobilebanking.platform.reconciliation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationScheduler {

    private final ReconciliationApplicationService applicationService;

    @Scheduled(cron = "${bank.reconciliation.cron:0 0 2 * * *}", zone = "${bank.transfer.zone-id:Asia/Shanghai}")
    public void runDaily() {
        try {
            var result = applicationService.run(null, "scheduler");
            log.info("Daily reconciliation completed: batchId={}, status={}, mismatchCount={}",
                    result.id(), result.status(), result.mismatchCount());
        } catch (RuntimeException exception) {
            log.error("Daily reconciliation failed", exception);
        }
    }
}
