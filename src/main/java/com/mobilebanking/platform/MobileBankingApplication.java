package com.mobilebanking.platform;

import com.mobilebanking.platform.config.BankProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableScheduling
@EnableConfigurationProperties(BankProperties.class)
public class MobileBankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MobileBankingApplication.class, args);
    }
}
