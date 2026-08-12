package com.mobilebanking.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "bank")
public class BankProperties {

    private boolean demoEnabled = true;
    private final Security security = new Security();
    private final Transfer transfer = new Transfer();
    private final Reconciliation reconciliation = new Reconciliation();

    @Getter
    @Setter
    public static class Security {
        private Duration tokenTtl = Duration.ofHours(8);
        private int loginMaxFailures = 5;
        private Duration loginWindow = Duration.ofMinutes(10);
        private boolean redisRateLimitEnabled = false;
    }

    @Getter
    @Setter
    public static class Transfer {
        private BigDecimal singleLimit = new BigDecimal("50000.00");
        private BigDecimal dailyLimit = new BigDecimal("100000.00");
        private String zoneId = "Asia/Shanghai";
        private int idempotencyKeyMinLength = 8;
        private int idempotencyKeyMaxLength = 64;
        private Duration processingTimeout = Duration.ofMinutes(5);
    }

    @Getter
    @Setter
    public static class Reconciliation {
        private String cron = "0 0 2 * * *";
    }
}
