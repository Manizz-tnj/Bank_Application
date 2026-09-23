package com.bank.security;

import com.bank.enums.AlertSeverity;
import com.bank.enums.AlertStatus;
import com.bank.exceptions.AccountNotFoundException;
import com.bank.exceptions.BankingException;
import com.bank.exceptions.CustomerNotFoundException;
import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.SecurityAlert;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.SecurityAlertRepository;
import com.bank.util.IdGenerator;

import java.util.List;
import java.util.Optional;

/**
 * Service managing administrative and employee security actions,
 * account lock/unlock enforcement, and security alert auditing.
 */
public class SecurityService {

    private final SecurityAlertRepository alertRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public SecurityService(SecurityAlertRepository alertRepository,
                           AccountRepository accountRepository,
                           CustomerRepository customerRepository) {
        this.alertRepository = alertRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public void lockAccount(String accountId, String reason, String initiatedBy) throws BankingException {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found.", accountId));

        account.lockAccount(reason);
        accountRepository.save(account);

        logSecurityEvent(account.getCustomerId(), accountId, AlertSeverity.HIGH,
                "MANUAL_ACCOUNT_LOCK",
                "Account " + accountId + " locked by " + initiatedBy + ". Reason: " + reason);
    }

    public void unlockAccount(String accountId, String initiatedBy) throws BankingException {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found.", accountId));

        account.unlockAccount();
        accountRepository.save(account);

        logSecurityEvent(account.getCustomerId(), accountId, AlertSeverity.MEDIUM,
                "MANUAL_ACCOUNT_UNLOCK",
                "Account " + accountId + " unlocked by " + initiatedBy);
    }

    public void lockCustomer(String customerId, String reason) throws BankingException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer " + customerId + " not found.", customerId));

        customer.lock(reason);
        customerRepository.save(customer);

        logSecurityEvent(customerId, null, AlertSeverity.CRITICAL,
                "CUSTOMER_PROFILE_LOCKED",
                "Customer profile " + customerId + " locked. Reason: " + reason);
    }

    public void unlockCustomer(String customerId) throws BankingException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer " + customerId + " not found.", customerId));

        customer.unlock();
        customerRepository.save(customer);

        logSecurityEvent(customerId, null, AlertSeverity.LOW,
                "CUSTOMER_PROFILE_UNLOCKED",
                "Customer profile " + customerId + " unlocked.");
    }

    public void logSecurityEvent(String customerId, String accountId, AlertSeverity severity,
                                 String ruleName, String description) {
        SecurityAlert alert = new SecurityAlert(
                IdGenerator.generateAlertId(),
                customerId,
                accountId,
                severity,
                ruleName,
                description
        );
        alertRepository.save(alert);
    }

    public List<SecurityAlert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public List<SecurityAlert> getAlertsBySeverity(AlertSeverity severity) {
        return alertRepository.findBySeverity(severity);
    }

    public Optional<SecurityAlert> getAlertById(String alertId) {
        return alertRepository.findById(alertId);
    }

    public void resolveAlert(String alertId, String resolutionNotes) throws BankingException {
        SecurityAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new BankingException("Alert " + alertId + " not found."));
        alert.resolve(resolutionNotes);
        alertRepository.save(alert);
    }
}
