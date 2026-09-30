package com.bank.service;

import com.bank.entity.Account;
import com.bank.entity.Customer;
import com.bank.entity.SecurityAlert;
import com.bank.entity.Transaction;
import com.bank.enums.AlertSeverity;
import com.bank.repository.SecurityAlertRepository;
import com.bank.repository.TransactionRepository;
import com.bank.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudDetectionService {

    public static final double LARGE_TRANSACTION_THRESHOLD = 50000.0;
    public static final int MAX_FAILED_PIN_ATTEMPTS = 3;
    public static final int RAPID_TRANSFER_LIMIT = 3;
    public static final int RAPID_TRANSFER_WINDOW_MINUTES = 5;

    private final SecurityAlertRepository alertRepository;
    private final TransactionRepository transactionRepository;

    public FraudDetectionService(SecurityAlertRepository alertRepository,
                                 TransactionRepository transactionRepository) {
        this.alertRepository = alertRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Rule 1: Large Transaction Threshold Detection
     */
    public void evaluateLargeTransaction(Account account, Transaction txn) {
        if (txn != null && txn.getAmount() >= LARGE_TRANSACTION_THRESHOLD) {
            String custId = account.getCustomer() != null ? account.getCustomer().getCustomerId() : null;
            String desc = String.format("High value %s of $%,.2f on account %s exceeds threshold ($%,.2f).",
                    txn.getTransactionType(), txn.getAmount(), account.getAccountId(), LARGE_TRANSACTION_THRESHOLD);

            SecurityAlert alert = new SecurityAlert(
                    IdGenerator.generateAlertId(),
                    custId,
                    account.getAccountId(),
                    AlertSeverity.HIGH,
                    "LARGE_TRANSACTION_THRESHOLD",
                    desc
            );
            alertRepository.save(alert);
        }
    }

    /**
     * Rule 2: Repeated Failed PIN Detection
     */
    public void evaluateFailedLoginAttempts(Customer customer) {
        if (customer == null) return;
        int attempts = customer.getFailedLoginAttempts();

        if (attempts >= MAX_FAILED_PIN_ATTEMPTS) {
            String desc = String.format("Account lockout triggered for %s after %d consecutive failed PIN attempts.",
                    customer.getCustomerId(), attempts);
            SecurityAlert alert = new SecurityAlert(
                    IdGenerator.generateAlertId(),
                    customer.getCustomerId(),
                    null,
                    AlertSeverity.CRITICAL,
                    "EXCESSIVE_FAILED_PIN",
                    desc
            );
            alertRepository.save(alert);
        } else if (attempts >= 2) {
            String desc = String.format("Multiple failed PIN attempts (%d) detected for %s.",
                    attempts, customer.getCustomerId());
            SecurityAlert alert = new SecurityAlert(
                    IdGenerator.generateAlertId(),
                    customer.getCustomerId(),
                    null,
                    AlertSeverity.MEDIUM,
                    "EXCESSIVE_FAILED_PIN",
                    desc
            );
            alertRepository.save(alert);
        }
    }

    /**
     * Rule 3: Rapid Fund Transfer Velocity Check
     */
    public void evaluateTransferVelocity(Account account) {
        if (account == null) return;
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(RAPID_TRANSFER_WINDOW_MINUTES);
        List<Transaction> recentTransfers = transactionRepository
                .findRecentTransfersByAccountId(account.getAccountId(), cutoff);

        if (recentTransfers.size() >= RAPID_TRANSFER_LIMIT) {
            String custId = account.getCustomer() != null ? account.getCustomer().getCustomerId() : null;
            String desc = String.format("High transfer velocity detected: %d transfers executed within %d minutes on account %s.",
                    recentTransfers.size(), RAPID_TRANSFER_WINDOW_MINUTES, account.getAccountId());

            SecurityAlert alert = new SecurityAlert(
                    IdGenerator.generateAlertId(),
                    custId,
                    account.getAccountId(),
                    AlertSeverity.HIGH,
                    "RAPID_TRANSFER_VELOCITY",
                    desc
            );
            alertRepository.save(alert);
        }
    }

    /**
     * Rule 4: Unusual Same-Day Capital Drain Check
     */
    public void evaluateUnusualActivity(Account account, Transaction txn) {
        if (account != null && txn != null) {
            boolean createdToday = account.getCreatedAt().equals(LocalDate.now());
            if (createdToday && account.getBalance() > 0) {
                double total = account.getBalance() + txn.getAmount();
                double drainRatio = txn.getAmount() / total;
                if (drainRatio >= 0.90 && txn.getAmount() > 5000.0) {
                    String custId = account.getCustomer() != null ? account.getCustomer().getCustomerId() : null;
                    String desc = String.format("Rapid capital drain of $%,.2f (%.1f%% of funds) from freshly opened account %s.",
                            txn.getAmount(), (drainRatio * 100), account.getAccountId());

                    SecurityAlert alert = new SecurityAlert(
                            IdGenerator.generateAlertId(),
                            custId,
                            account.getAccountId(),
                            AlertSeverity.MEDIUM,
                            "UNUSUAL_ACCOUNT_ACTIVITY",
                            desc
                    );
                    alertRepository.save(alert);
                }
            }
        }
    }
}
