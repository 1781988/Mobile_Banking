package com.mobilebanking.platform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "bank.demo-enabled=false",
        "bank.security.redis-rate-limit-enabled=false",
        "management.health.redis.enabled=false"
})
@EnabledIfEnvironmentVariable(named = "CI_MYSQL", matches = "true")
class MySqlMigrationTest {

    @Test
    void flywayMigrationAndJpaValidationSucceed() {
    }
}
