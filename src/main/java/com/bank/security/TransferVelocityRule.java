package com.bank.security;

import com.bank.enums.AlertSeverity;
import com.bank.model.Bank;
import com.bank.model.SecurityAlert;
import com.bank.model.Transaction;
import com.bank.util.IdGenerator;

import java.util.List;
import java.util.Optional;

/**
 * Rule 3: Flags rapid succession of fund transfers occurring within a short time window.
 */
public class TransferVelocityRule implements FraudRule {

    private final int velocityLimit;
    private final int windowMinutes;

    public TransferVelocityRule() {
        this(Bank.RAPID_TRANSFER_VELOCITY_LIMIT, Bank.RAPID_TRANSFER_WINDOW_MINUTES);
    }

    public TransferVelocityRule(int velocityLimit, int windowMinutes) {
        this.velocityLimit = velocityLimit;
        this.windowMinutes = windowMinutes;
    }

    @Override
    public String getRuleName() {
        return "RAPID_TRANSFER_VELOCITY";
    }

    @Override
    public Optional<SecurityAlert> evaluate(FraudContext context) {
        List<Transaction> recent = context.getRecentTransfers();
        if (recent != null && recent.size() >= velocityLimit) {
            String accId = context.getAccount() != null ? context.getAccount().getAccountId() :
                    (context.getTransaction() != null ? context.getTransaction().getAccountId() : "UNKNOWN");
            String custId = context.getCustomer() != null ? context.getCustomer().getId() : null;
            String desc = String.format("High transfer velocity detected: %d transfers executed within %d minutes on account %s.",
                    recent.size(), windowMinutes, accId);
            SecurityAlert alert = new SecurityAlert(
                    IdGenerator.generateAlertId(),
                    custId,
                    accId,
                    AlertSeverity.HIGH,
                    getRuleName(),
                    desc
            );
            return Optional.of(alert);
        }
        return Optional.empty();
    }
}
