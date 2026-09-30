package com.bank;

import com.bank.dto.*;
import com.bank.entity.Account;
import com.bank.entity.LedgerEntry;
import com.bank.enums.AccountType;
import com.bank.enums.AlertSeverity;
import com.bank.enums.LedgerEntryType;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.LedgerEntryRepository;
import com.bank.repository.SecurityAlertRepository;
import com.bank.service.AccountService;
import com.bank.service.AuthenticationService;
import com.bank.service.CustomerService;
import com.bank.service.TransactionService;
import com.bank.util.PasswordValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class BankApplicationTests {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private SecurityAlertRepository alertRepository;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(customerService);
        Assertions.assertNotNull(accountService);
        Assertions.assertNotNull(transactionService);
        Assertions.assertNotNull(ledgerEntryRepository);
    }

    @Test
    void testCustomerRegistrationAndLogin() {
        CustomerRequest req = new CustomerRequest(
                "Clark Kent",
                "clark@dailyplanet.com",
                "18005550022",
                "344 Clinton St, Metropolis",
                LocalDate.of(1990, 6, 18),
                "DL-MET-88912",
                "8392",
                AccountType.SAVINGS,
                2500.0
        );
        CustomerResponse res = customerService.registerCustomer(req);

        Assertions.assertNotNull(res);
        Assertions.assertTrue(res.getCustomerId().startsWith("CUST-"));
        Assertions.assertEquals("Clark Kent", res.getName());
        Assertions.assertEquals("KYC_VERIFIED", res.getKycStatus());
        Assertions.assertEquals("APEX000101", res.getRoutingNumber());
        Assertions.assertFalse(res.getAccountIds().isEmpty());

        LoginResponse loginRes = authService.login(new LoginRequest(res.getCustomerId(), "8392"));
        Assertions.assertTrue(loginRes.isSuccess());
        Assertions.assertEquals(res.getCustomerId(), loginRes.getUserId());
        Assertions.assertNotNull(loginRes.getToken(), "JWT token must be generated upon successful login");
    }

    @Test
    void testAccountCreationDepositAndTransferWithDoubleEntryLedger() {
        CustomerRequest cust1Req = new CustomerRequest(
                "Diana Prince",
                "diana@themyscira.gov",
                "18005550033",
                "Gateway City",
                LocalDate.of(1985, 3, 22),
                "PASSPORT-TH-11",
                "5829",
                AccountType.SAVINGS,
                5000.0
        );
        CustomerResponse cust1 = customerService.registerCustomer(cust1Req);

        CustomerRequest cust2Req = new CustomerRequest(
                "Barry Allen",
                "barry@ccpd.gov",
                "18005550044",
                "Central City",
                LocalDate.of(1992, 9, 30),
                "DL-CC-9921",
                "9147",
                AccountType.CURRENT,
                2000.0
        );
        CustomerResponse cust2 = customerService.registerCustomer(cust2Req);

        String acc1Id = cust1.getAccountIds().get(0);
        String acc2Id = cust2.getAccountIds().get(0);

        // Deposit into acc1
        AccountResponse afterDeposit = accountService.deposit(acc1Id, new DepositWithdrawRequest(1500.0, "Bonus"));
        Assertions.assertEquals(6500.0, afterDeposit.getBalance());

        // Transfer from acc1 to acc2 with Idempotency Key
        String idempotencyKey = UUID.randomUUID().toString();
        TransferRequest transferReq = new TransferRequest(acc1Id, acc2Id, 1000.0, "Interbank Transfer", idempotencyKey);
        TransactionResponse txnRes = transactionService.transfer(transferReq, idempotencyKey);

        Assertions.assertNotNull(txnRes);
        Account updatedAcc1 = accountRepository.findByAccountId(acc1Id).orElseThrow();
        Account updatedAcc2 = accountRepository.findByAccountId(acc2Id).orElseThrow();

        Assertions.assertEquals(5500.0, updatedAcc1.getBalance());
        Assertions.assertEquals(3000.0, updatedAcc2.getBalance());

        // Test Idempotent replay: Re-sending exact same transfer request with same key should NOT double debit
        TransactionResponse replayRes = transactionService.transfer(transferReq, idempotencyKey);
        Assertions.assertEquals(txnRes.getTransactionId(), replayRes.getTransactionId());

        Account afterReplayAcc1 = accountRepository.findByAccountId(acc1Id).orElseThrow();
        Assertions.assertEquals(5500.0, afterReplayAcc1.getBalance(), "Idempotent replay must not deduct balance again");

        // Verify Double-Entry Ledger entries
        List<LedgerEntry> acc1Ledgers = ledgerEntryRepository.findByAccountIdOrderByTimestampAsc(acc1Id);
        Assertions.assertFalse(acc1Ledgers.isEmpty(), "Account 1 must have ledger entries");

        // Reconcile balance from ledger entries
        Double reconciledAcc1 = ledgerEntryRepository.calculateReconciledBalance(acc1Id);
        Assertions.assertEquals(5500.0, reconciledAcc1, 0.001, "Reconciled ledger balance must match account balance");
    }

    @Test
    void testPasswordAndPinSecurityValidation() {
        // Trivial PINs should fail
        Assertions.assertThrows(IllegalArgumentException.class, () -> PasswordValidator.validatePin("1234"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> PasswordValidator.validatePin("0000"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> PasswordValidator.validatePin("4321"));

        // Valid PIN should succeed
        Assertions.assertDoesNotThrow(() -> PasswordValidator.validatePin("9284"));

        // Weak passwords should fail
        Assertions.assertThrows(IllegalArgumentException.class, () -> PasswordValidator.validatePassword("weak"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> PasswordValidator.validatePassword("nouppercase1!"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> PasswordValidator.validatePassword("NOLOWERCASE1!"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> PasswordValidator.validatePassword("NoDigitsHere!"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> PasswordValidator.validatePassword("NoSpecialChars123"));

        // Strong enterprise password should succeed
        Assertions.assertDoesNotThrow(() -> PasswordValidator.validatePassword("Admin@Apex2026!"));
    }

    @Test
    void testFraudRuleLargeTransaction() {
        CustomerRequest req = new CustomerRequest(
                "Lex Luthor",
                "lex@lexcorp.com",
                "18005550100",
                "LexCorp Metropolis",
                LocalDate.of(1980, 11, 5),
                "TAX-LX-1000",
                "6294",
                AccountType.CURRENT,
                10000.0
        );
        CustomerResponse cust = customerService.registerCustomer(req);
        String accId = cust.getAccountIds().get(0);

        // Deposit >= $50,000 to trigger large transaction rule
        accountService.deposit(accId, new DepositWithdrawRequest(75000.0, "Offshore Liquidity"));

        boolean highAlertFound = alertRepository.findBySeverity(AlertSeverity.HIGH).stream()
                .anyMatch(a -> "LARGE_TRANSACTION_THRESHOLD".equals(a.getRuleTriggered()));
        Assertions.assertTrue(highAlertFound, "Fraud rule must trigger high severity alert on large transactions");
    }
}
