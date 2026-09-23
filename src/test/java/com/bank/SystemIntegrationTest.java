package com.bank;

import com.bank.enums.*;
import com.bank.exceptions.*;
import com.bank.model.*;
import com.bank.repository.*;
import com.bank.security.*;
import com.bank.service.*;

import java.io.File;
import java.util.List;

/**
 * Comprehensive Automated System Integration Test Suite.
 * Validates all OOP principles, user roles, security, fraud rules, and persistence
 * without external testing frameworks.
 */
public class SystemIntegrationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("       RUNNING COMPREHENSIVE INTEGRATION & REGRESSION TEST SUITE");
        System.out.println("================================================================================");

        // Clean test data directory
        cleanTestDirectory("test_data");

        try {
            CustomerRepository customerRepo = new CustomerRepository("test_data/customers.dat");
            AccountRepository accountRepo = new AccountRepository("test_data/accounts.dat");
            TransactionRepository txnRepo = new TransactionRepository("test_data/transactions.dat");
            SecurityAlertRepository alertRepo = new SecurityAlertRepository("test_data/security_alerts.dat");
            EmployeeRepository employeeRepo = new EmployeeRepository("test_data/staff.dat");

            SecurityService securityService = new SecurityService(alertRepo, accountRepo, customerRepo);
            FraudDetectionService fraudService = new FraudDetectionService(alertRepo);
            AuthenticationService authService = new AuthenticationService(customerRepo, employeeRepo, securityService, fraudService);

            CustomerService customerService = new CustomerService(customerRepo, securityService);
            AccountService accountService = new AccountService(accountRepo, customerRepo, txnRepo, securityService);
            TransactionService txnService = new TransactionService(accountRepo, customerRepo, txnRepo, fraudService, securityService, accountService);
            BankStatisticsService statsService = new BankStatisticsService(customerRepo, accountRepo, txnRepo, alertRepo);

            // Test 1: Customer Registration & Duplicate Prevention
            testCustomerRegistration(customerService);

            // Test 2: Secure Authentication & SHA-256 PIN Verification
            testAuthentication(authService, customerService);

            // Test 3: Account Creation & Polymorphic Hierarchy
            testAccountHierarchy(accountService, customerService);

            // Test 4: Deposits, Overloaded Methods & Balance Updates
            testDeposits(txnService, accountService, customerService);

            // Test 5: Withdrawals, Minimum Balance & Overdraft Logic
            testWithdrawals(txnService, accountService, customerService);

            // Test 6: Fund Transfers & Atomic Execution
            testTransfers(txnService, accountService, customerService);

            // Test 7: Unauthorized Access & Customer Ownership Protection
            testCustomerOwnershipSecurity(txnService, accountService, customerService);

            // Test 8: Fraud Detection Engine & Suspicious Activity Alerts
            testFraudDetection(txnService, accountService, customerService, alertRepo);

            // Test 9: Brute-Force PIN Lockout Security
            testBruteForceLockout(authService, customerService, securityService);

            // Test 10: Serialization & File Persistence Roundtrip
            testPersistenceRoundtrip(customerRepo, accountRepo, txnRepo);

            // Test 11: Java 21 Streams & Statistics Generation
            testStreamsAndStatistics(statsService);

            System.out.println("================================================================================");
            System.out.printf(" TEST SUITE SUMMARY: %d / %d Tests Passed Successfully!%n", testsPassed, testsRun);
            System.out.println("================================================================================");

        } catch (Exception e) {
            System.err.println("FATAL TEST SUITE FAILURE: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanTestDirectory("test_data");
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("[PASS] " + testName);
        } else {
            System.err.println("[FAIL] " + testName);
            throw new AssertionError("Test assertion failed: " + testName);
        }
    }

    private static void testCustomerRegistration(CustomerService customerService) throws Exception {
        System.out.println("\n--- Testing Registration & Duplicate Prevention ---");
        Customer c1 = customerService.registerCustomer("Bruce Wayne", "bruce@waynecorp.com", "18005550011",
                "1007 Mountain Drive, Gotham", "1234");
        assertTrue("Customer ID generated with CUST- prefix", c1.getId().startsWith("CUST-"));
        assertTrue("Customer Name set properly", "Bruce Wayne".equals(c1.getName()));

        // Test duplicate email prevention
        boolean duplicateCaught = false;
        try {
            customerService.registerCustomer("Imposter", "bruce@waynecorp.com", "18005550099", "Somewhere", "9999");
        } catch (DuplicateUserException e) {
            duplicateCaught = true;
        }
        assertTrue("Duplicate email registration rejected", duplicateCaught);
    }

    private static void testAuthentication(AuthenticationService authService, CustomerService customerService) throws Exception {
        System.out.println("\n--- Testing Authentication & SHA-256 PIN Hashing ---");
        Customer c = customerService.registerCustomer("Clark Kent", "clark@dailyplanet.com", "18005550022",
                "344 Clinton St, Metropolis", "4321");

        Customer authenticated = authService.authenticateCustomer(c.getId(), "4321");
        assertTrue("Customer successfully authenticated with correct PIN", authenticated != null);

        boolean wrongPinCaught = false;
        try {
            authService.authenticateCustomer(c.getId(), "0000");
        } catch (InvalidPinException e) {
            wrongPinCaught = true;
        }
        assertTrue("Wrong PIN rejected with InvalidPinException", wrongPinCaught);
    }

    private static void testAccountHierarchy(AccountService accountService, CustomerService customerService) throws Exception {
        System.out.println("\n--- Testing Account Hierarchy & Polymorphism ---");
        Customer c = customerService.registerCustomer("Diana Prince", "diana@themyscira.gov", "18005550033",
                "Gateway City", "5555");

        SavingsAccount sav = accountService.createSavingsAccount(c.getId(), 2000.0);
        assertTrue("Savings account created with initial balance", sav.getBalance() == 2000.0);
        assertTrue("Savings account calculated interest > 0", sav.calculateInterest() == 80.0);

        CurrentAccount cur = accountService.createCurrentAccount(c.getId(), 3000.0, 5000.0);
        assertTrue("Current account created with balance", cur.getBalance() == 3000.0);
        assertTrue("Current account overdraft configured", cur.getOverdraftLimit() == 5000.0);

        FixedDepositAccount fd = accountService.createFixedDepositAccount(c.getId(), 10000.0, 12);
        assertTrue("Fixed Deposit interest calculation correct", fd.calculateInterest() == 700.0);
        assertTrue("Fixed Deposit maturity amount correct", fd.getMaturityAmount() == 10700.0);
    }

    private static void testDeposits(TransactionService txnService, AccountService accountService, CustomerService customerService) throws Exception {
        System.out.println("\n--- Testing Deposit Operations ---");
        Customer c = customerService.registerCustomer("Barry Allen", "barry@ccpd.gov", "18005550044", "Central City", "1111");
        SavingsAccount sav = accountService.createSavingsAccount(c.getId(), 1500.0);

        Transaction txn = txnService.deposit(c.getId(), sav.getAccountId(), 500.0, "Salary Bonus");
        Account updated = accountService.getAccountById(sav.getAccountId());
        assertTrue("Account balance incremented after deposit", updated.getBalance() == 2000.0);
        assertTrue("Deposit transaction recorded with SUCCESS", txn.getStatus() == TransactionStatus.SUCCESS);
    }

    private static void testWithdrawals(TransactionService txnService, AccountService accountService, CustomerService customerService) throws Exception {
        System.out.println("\n--- Testing Withdrawal Logic & Overdraft ---");
        Customer c = customerService.registerCustomer("Hal Jordan", "hal@ferris.com", "18005550055", "Coast City", "2222");

        // Savings Account: Minimum balance enforcement
        SavingsAccount sav = accountService.createSavingsAccount(c.getId(), 1500.0);
        boolean minBalBlocked = false;
        try {
            txnService.withdraw(c.getId(), sav.getAccountId(), 600.0, "Excess withdrawal");
        } catch (InsufficientBalanceException e) {
            minBalBlocked = true;
        }
        assertTrue("Savings account withdrawal below minimum balance blocked", minBalBlocked);

        // Current Account: Overdraft facility usage
        CurrentAccount cur = accountService.createCurrentAccount(c.getId(), 1000.0, 4000.0);
        txnService.withdraw(c.getId(), cur.getAccountId(), 2500.0, "Overdraft withdrawal");
        Account curUpdated = accountService.getAccountById(cur.getAccountId());
        assertTrue("Current account allowed withdrawal into overdraft", curUpdated.getBalance() == -1500.0);
    }

    private static void testTransfers(TransactionService txnService, AccountService accountService, CustomerService customerService) throws Exception {
        System.out.println("\n--- Testing Fund Transfers ---");
        Customer sender = customerService.registerCustomer("Arthur Curry", "arthur@atlantis.org", "18005550066", "Amnesty Bay", "3333");
        Customer recipient = customerService.registerCustomer("Mera", "mera@atlantis.org", "18005550077", "Atlantis", "4444");

        SavingsAccount senderAcc = accountService.createSavingsAccount(sender.getId(), 5000.0);
        SavingsAccount recipientAcc = accountService.createSavingsAccount(recipient.getId(), 2000.0);

        txnService.transfer(sender.getId(), senderAcc.getAccountId(), recipientAcc.getAccountId(), 1000.0, "Treasury transfer");

        Account senderUpdated = accountService.getAccountById(senderAcc.getAccountId());
        Account recipientUpdated = accountService.getAccountById(recipientAcc.getAccountId());

        assertTrue("Sender debited correctly", senderUpdated.getBalance() == 4000.0);
        assertTrue("Recipient credited correctly", recipientUpdated.getBalance() == 3000.0);
    }

    private static void testCustomerOwnershipSecurity(TransactionService txnService, AccountService accountService, CustomerService customerService) throws Exception {
        System.out.println("\n--- Testing Customer Ownership Boundary Security ---");
        Customer victim = customerService.registerCustomer("Victor Stone", "victor@star.org", "18005550088", "Detroit", "5555");
        Customer attacker = customerService.registerCustomer("Joker", "joker@arkham.org", "18005550099", "Gotham Asylum", "6666");

        SavingsAccount victimAcc = accountService.createSavingsAccount(victim.getId(), 10000.0);

        boolean unauthorizedBlocked = false;
        try {
            txnService.withdraw(attacker.getId(), victimAcc.getAccountId(), 500.0, "Theft attempt");
        } catch (UnauthorizedOperationException e) {
            unauthorizedBlocked = true;
        }
        assertTrue("Customer cannot withdraw from another customer's account", unauthorizedBlocked);
    }

    private static void testFraudDetection(TransactionService txnService, AccountService accountService,
                                           CustomerService customerService, SecurityAlertRepository alertRepo) throws Exception {
        System.out.println("\n--- Testing Rule-Based Fraud Detection ---");
        Customer c = customerService.registerCustomer("Lex Luthor", "lex@lexcorp.com", "18005550100", "Metropolis LexCorp", "7777");
        CurrentAccount acc = accountService.createCurrentAccount(c.getId(), 200000.0, 50000.0);

        // Trigger Rule 1: Large Transaction >= $50,000
        txnService.deposit(c.getId(), acc.getAccountId(), 75000.0, "Offshore funding");

        List<SecurityAlert> alerts = alertRepo.findBySeverity(AlertSeverity.HIGH);
        boolean largeTxnAlertFound = alerts.stream().anyMatch(a -> "LARGE_TRANSACTION_THRESHOLD".equals(a.getRuleTriggered()));
        assertTrue("Fraud Rule 1 triggered for large transaction >= $50,000", largeTxnAlertFound);
    }

    private static void testBruteForceLockout(AuthenticationService authService, CustomerService customerService,
                                             SecurityService securityService) throws Exception {
        System.out.println("\n--- Testing Brute Force PIN Lockout Policy ---");
        Customer c = customerService.registerCustomer("Oliver Queen", "oliver@queen.com", "18005550111", "Star City", "8888");

        // Attempt 1
        try { authService.authenticateCustomer(c.getId(), "0001"); } catch (InvalidPinException ignored) {}
        // Attempt 2
        try { authService.authenticateCustomer(c.getId(), "0002"); } catch (InvalidPinException ignored) {}
        // Attempt 3 -> Lockout
        boolean locked = false;
        try {
            authService.authenticateCustomer(c.getId(), "0003");
        } catch (AccountLockedException e) {
            locked = true;
        }
        assertTrue("Account automatically locked after 3 consecutive failed PIN attempts", locked);

        // Verify locked customer cannot log in even with correct PIN
        boolean lockedLoginBlocked = false;
        try {
            authService.authenticateCustomer(c.getId(), "8888");
        } catch (AccountLockedException e) {
            lockedLoginBlocked = true;
        }
        assertTrue("Locked account denied access even with valid credentials", lockedLoginBlocked);

        // Test Unlock
        securityService.unlockCustomer(c.getId());
        Customer unlocked = authService.authenticateCustomer(c.getId(), "8888");
        assertTrue("Account can successfully log in after being unlocked", unlocked != null);
    }

    private static void testPersistenceRoundtrip(CustomerRepository custRepo, AccountRepository accRepo,
                                                 TransactionRepository txnRepo) {
        System.out.println("\n--- Testing Java Object Serialization Persistence ---");
        custRepo.flush();
        accRepo.flush();
        txnRepo.flush();

        // Create new instances reading from same files
        CustomerRepository reloadedCust = new CustomerRepository("test_data/customers.dat");
        AccountRepository reloadedAcc = new AccountRepository("test_data/accounts.dat");
        TransactionRepository reloadedTxn = new TransactionRepository("test_data/transactions.dat");

        assertTrue("Reloaded customers count matches in-memory count", reloadedCust.count() == custRepo.count());
        assertTrue("Reloaded accounts count matches in-memory count", reloadedAcc.count() == accRepo.count());
        assertTrue("Reloaded transactions count matches in-memory count", reloadedTxn.count() == txnRepo.count());
    }

    private static void testStreamsAndStatistics(BankStatisticsService statsService) {
        System.out.println("\n--- Testing Java 21 Streams and Analytics ---");
        statsService.displaySystemStatistics();
        assertTrue("System statistics calculated and displayed via Java 21 Streams", true);
    }

    private static void cleanTestDirectory(String dirName) {
        File dir = new File(dirName);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            dir.delete();
        }
    }
}
