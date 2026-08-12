package com.mobilebanking.platform.bootstrap;

import com.mobilebanking.platform.account.domain.AccountStatus;
import com.mobilebanking.platform.account.domain.BankAccount;
import com.mobilebanking.platform.account.repository.BankAccountRepository;
import com.mobilebanking.platform.config.BankProperties;
import com.mobilebanking.platform.user.domain.AppUser;
import com.mobilebanking.platform.user.domain.UserRole;
import com.mobilebanking.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

    public static final String ALICE_ACCOUNT = "6222026000000001";
    public static final String BOB_ACCOUNT = "6222026000000002";

    private final BankProperties bankProperties;
    private final UserRepository userRepository;
    private final BankAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!bankProperties.isDemoEnabled()) {
            return;
        }
        AppUser alice = ensureUser("alice", "Alice Chen", UserRole.USER, "Bank@12345");
        AppUser bob = ensureUser("bob", "Bob Li", UserRole.USER, "Bank@12345");
        ensureUser("admin", "Risk Administrator", UserRole.ADMIN, "Admin@12345");

        ensureAccount(alice, ALICE_ACCOUNT, new BigDecimal("10000.00"));
        ensureAccount(bob, BOB_ACCOUNT, new BigDecimal("5000.00"));
        log.warn("Demo data is enabled. Disable BANK_DEMO_ENABLED in any non-demo environment.");
    }

    private AppUser ensureUser(String username,
                               String displayName,
                               UserRole role,
                               String rawPassword) {
        return userRepository.findByUsernameIgnoreCase(username).orElseGet(() -> {
            var now = clock.instant();
            AppUser user = new AppUser();
            user.setUsername(username);
            user.setDisplayName(displayName);
            user.setRole(role);
            user.setEnabled(true);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            return userRepository.save(user);
        });
    }

    private void ensureAccount(AppUser user, String accountNumber, BigDecimal openingBalance) {
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            return;
        }
        var now = clock.instant();
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setAccountNumber(accountNumber);
        account.setCurrency("CNY");
        account.setOpeningBalance(openingBalance);
        account.setBalance(openingBalance);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        accountRepository.save(account);
    }
}
