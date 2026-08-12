package com.mobilebanking.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilebanking.platform.account.domain.AccountStatus;
import com.mobilebanking.platform.account.domain.BankAccount;
import com.mobilebanking.platform.account.repository.BankAccountRepository;
import com.mobilebanking.platform.transfer.repository.LedgerEntryRepository;
import com.mobilebanking.platform.user.domain.AppUser;
import com.mobilebanking.platform.user.domain.UserRole;
import com.mobilebanking.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "bank.transfer.single-limit=5000.00",
        "bank.transfer.daily-limit=10000.00"
})
class TransferApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired BankAccountRepository accountRepository;
    @Autowired LedgerEntryRepository ledgerEntryRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String payerAccount;
    private String payeeAccount;

    @BeforeEach
    void seed() {
        payerAccount = "6222026000000101";
        payeeAccount = "6222026000000102";
        if (!accountRepository.existsByAccountNumber(payerAccount)) {
            AppUser payer = createUser("integration-payer", "Integration Payer");
            AppUser payee = createUser("integration-payee", "Integration Payee");
            createAccount(payer, payerAccount, "1000.00");
            createAccount(payee, payeeAccount, "500.00");
        }
    }

    @Test
    void shouldTransferExactlyOnceForSameIdempotencyKey() throws Exception {
        String token = login();
        String request = """
                {
                  "payerAccountNumber": "%s",
                  "payeeAccountNumber": "%s",
                  "amount": 100.00,
                  "remark": "integration test"
                }
                """.formatted(payerAccount, payeeAccount);

        mockMvc.perform(post("/api/v1/transfers")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "integration-key-0001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.data.replayed").value(false));

        mockMvc.perform(post("/api/v1/transfers")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "integration-key-0001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.data.replayed").value(true));

        assertThat(accountRepository.findByAccountNumber(payerAccount).orElseThrow().getBalance())
                .isEqualByComparingTo("900.00");
        assertThat(accountRepository.findByAccountNumber(payeeAccount).orElseThrow().getBalance())
                .isEqualByComparingTo("600.00");
        assertThat(ledgerEntryRepository.count()).isEqualTo(2);
    }

    private String login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"integration-payer","password":"Bank@12345"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.path("data").path("accessToken").asText();
    }

    private AppUser createUser(String username, String displayName) {
        Instant now = Instant.parse("2026-08-12T00:00:00Z");
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordEncoder.encode("Bank@12345"));
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }

    private void createAccount(AppUser user, String number, String balance) {
        Instant now = Instant.parse("2026-08-12T00:00:00Z");
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setAccountNumber(number);
        account.setCurrency("CNY");
        account.setOpeningBalance(new BigDecimal(balance));
        account.setBalance(new BigDecimal(balance));
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        accountRepository.save(account);
    }
}
