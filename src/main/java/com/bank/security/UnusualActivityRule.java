package com.bank.security;

import com.bank.enums.AlertSeverity;
import com.bank.model.Account;
import com.bank.model.SecurityAlert;
import com.bank.model.Transaction;
import com.bank.util.IdGenerator;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Rule 4: Flags unusual account behavior, such as draining >90% of funds
 * from an account created on the same day.
 */
public class UnusualActivityRule implements FraudRule {

    private static final double SAME_DAY_DRAIN_RATIO = 0.90;

    @Override
    public String getRuleName() {
        return "UNUSUAL_ACCOUNT_ACTIVITY";
    }

    @Override
    public Optional<SecurityAlert> evaluate(FraudContext context) {
        Account account = context.getAccount();
        Transaction txn = context.getTransaction();

        if (account != null && txn != null) {
            // Check if created today and withdrawing over 90% of current balance
            boolean createdToday = account.getCreatedDate().equals(LocalDate.now());
            if (createdToday && account.getBalance() > 0) {
                double drainRatio = txn.getAmount() / (account.getBalance() + txn.getAmount());
                if (drainRatio >= SAME_DAY_DRAIN_RATIO && txn.getAmount() > 5000.0) {
                    String custId = context.getCustomer() != null ? context.getCustomer().getId() : account.getCustomerId();
                    String desc = String.format("Rapid capital drain of $%,.2f (%.1f%% of funds) from freshly opened account %s.",
                            txn.getAmount(), (drainRatio * 100), account.getAccountId());
                    SecurityAlert alert = new SecurityAlert(
                            IdGenerator.generateAlertId(),
                            custId,
                            account.getAccountId(),
                            AlertSeverity.MEDIUM,
                            getRuleName(),
                            desc
                    );
                    return Optional.of(alert);
                }
            }
        }
        return Optional.empty();
    }
}
