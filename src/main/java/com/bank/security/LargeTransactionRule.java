package com.bank.security;

import com.bank.enums.AlertSeverity;
import com.bank.model.Bank;
import com.bank.model.SecurityAlert;
import com.bank.model.Transaction;
import com.bank.util.IdGenerator;

import java.util.Optional;

/**
 * Rule 1: Flags transactions exceeding the bank's configured large transaction threshold.
 */
public class LargeTransactionRule implements FraudRule {

    private final double threshold;

    public LargeTransactionRule() {
        this(Bank.LARGE_TRANSACTION_THRESHOLD);
    }

    public LargeTransactionRule(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public String getRuleName() {
        return "LARGE_TRANSACTION_THRESHOLD";
    }

    @Override
    public Optional<SecurityAlert> evaluate(FraudContext context) {
        Transaction txn = context.getTransaction();
        if (txn != null && txn.getAmount() >= threshold) {
            String custId = context.getCustomer() != null ? context.getCustomer().getId() : null;
            String desc = String.format("High value %s of $%,.2f on account %s exceeds threshold ($%,.2f).",
                    txn.getTransactionType(), txn.getAmount(), txn.getAccountId(), threshold);
            SecurityAlert alert = new SecurityAlert(
                    IdGenerator.generateAlertId(),
                    custId,
                    txn.getAccountId(),
                    AlertSeverity.HIGH,
                    getRuleName(),
                    desc
            );
            return Optional.of(alert);
        }
        return Optional.empty();
    }
}
