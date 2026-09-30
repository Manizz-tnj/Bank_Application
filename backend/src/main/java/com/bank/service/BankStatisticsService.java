package com.bank.service;

import com.bank.dto.SystemStatsResponse;
import com.bank.entity.Account;
import com.bank.entity.SecurityAlert;
import com.bank.entity.Transaction;
import com.bank.enums.AccountStatus;
import com.bank.enums.AlertSeverity;
import com.bank.enums.TransactionType;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.SecurityAlertRepository;
import com.bank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
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

    public SystemStatsResponse getStatistics() {
        long totalCustomers = customerRepository.count();
        List<Account> allAccounts = accountRepository.findAll();
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<SecurityAlert> allAlerts = alertRepository.findAll();

        Map<AccountStatus, Long> statusCounts = allAccounts.stream()
                .collect(Collectors.groupingBy(Account::getStatus, Collectors.counting()));

        long active = statusCounts.getOrDefault(AccountStatus.ACTIVE, 0L);
        long locked = statusCounts.getOrDefault(AccountStatus.LOCKED, 0L);
        long closed = statusCounts.getOrDefault(AccountStatus.CLOSED, 0L);

        double totalLiquidity = allAccounts.stream()
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .mapToDouble(Account::getBalance)
                .sum();

        double depositVolume = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.DEPOSIT)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double withdrawVolume = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.WITHDRAWAL)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double transferVolume = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.TRANSFER)
                .mapToDouble(Transaction::getAmount)
                .sum();

        long highAlerts = allAlerts.stream()
                .filter(a -> a.getSeverity() == AlertSeverity.HIGH)
                .count();

        long criticalAlerts = allAlerts.stream()
                .filter(a -> a.getSeverity() == AlertSeverity.CRITICAL)
                .count();

        return new SystemStatsResponse(
                totalCustomers,
                allAccounts.size(),
                active,
                locked,
                closed,
                totalLiquidity,
                allTransactions.size(),
                depositVolume,
                withdrawVolume,
                transferVolume,
                allAlerts.size(),
                highAlerts,
                criticalAlerts
        );
    }
}
