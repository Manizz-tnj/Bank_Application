package com.bank.service;

import com.bank.enums.AccountStatus;
import com.bank.enums.AlertSeverity;
import com.bank.enums.TransactionType;
import com.bank.model.Account;
import com.bank.model.SecurityAlert;
import com.bank.model.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.SecurityAlertRepository;
import com.bank.repository.TransactionRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service aggregating system-wide metrics and analytical statistics.
 * Demonstrates extensive usage of Java 21 Streams, Lambdas, and Collectors.
 */
public class BankStatisticsService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SecurityAlertRepository alertRepository;

    public BankStatisticsService(CustomerRepository customerRepository,
                                 AccountRepository accountRepository,
                                 TransactionRepository transactionRepository,
                                 SecurityAlertRepository alertRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
    }

    public void displaySystemStatistics() {
        long totalCustomers = customerRepository.count();
        List<Account> allAccounts = accountRepository.findAll();
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<SecurityAlert> allAlerts = alertRepository.findAll();

        // Account status grouping via Streams
        Map<AccountStatus, Long> accountsByStatus = allAccounts.stream()
                .collect(Collectors.groupingBy(Account::getStatus, Collectors.counting()));

        long activeAccounts = accountsByStatus.getOrDefault(AccountStatus.ACTIVE, 0L);
        long lockedAccounts = accountsByStatus.getOrDefault(AccountStatus.LOCKED, 0L);
        long closedAccounts = accountsByStatus.getOrDefault(AccountStatus.CLOSED, 0L);

        // Balance calculation via Streams
        double totalBankLiquidity = allAccounts.stream()
                .filter(Account::isActive)
                .mapToDouble(Account::getBalance)
                .sum();

        // Transaction volume by type via Streams
        double totalDeposited = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.DEPOSIT)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalWithdrawn = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.WITHDRAWAL)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalTransferred = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.TRANSFER)
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Alert statistics via Streams
        long highAlerts = allAlerts.stream()
                .filter(a -> a.getSeverity() == AlertSeverity.HIGH)
                .count();

        long criticalAlerts = allAlerts.stream()
                .filter(a -> a.getSeverity() == AlertSeverity.CRITICAL)
                .count();

        System.out.println("================================================================================");
        System.out.println("                     SYSTEM ANALYTICS & BANK STATISTICS");
        System.out.println("================================================================================");
        System.out.printf(" Total Registered Customers : %,d%n", totalCustomers);
        System.out.printf(" Total Bank Accounts        : %,d  (Active: %,d | Locked: %,d | Closed: %,d)%n",
                allAccounts.size(), activeAccounts, lockedAccounts, closedAccounts);
        System.out.printf(" Total System Balance       : $%,.2f%n", totalBankLiquidity);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf(" Total Transactions Logged  : %,d%n", allTransactions.size());
        System.out.printf("   - Total Deposits Volume  : $%,.2f%n", totalDeposited);
        System.out.printf("   - Total Withdraw Volume  : $%,.2f%n", totalWithdrawn);
        System.out.printf("   - Total Transfer Volume  : $%,.2f%n", totalTransferred);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf(" Total Security Alerts      : %,d%n", allAlerts.size());
        System.out.printf("   - High Severity Alerts   : %,d%n", highAlerts);
        System.out.printf("   - Critical Alerts        : %,d%n", criticalAlerts);
        System.out.println("================================================================================");
    }
}
