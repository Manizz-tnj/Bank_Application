package com.bank.security;

import com.bank.enums.AlertSeverity;
import com.bank.model.Bank;
import com.bank.model.SecurityAlert;
import com.bank.util.IdGenerator;

import java.util.Optional;

/**
 * Rule 2: Flags repeated failed PIN authentication attempts.
 * Generates CRITICAL severity alert when lock threshold is reached.
 */
public class FailedPinAttemptRule implements FraudRule {

    private final int maxAttempts;

    public FailedPinAttemptRule() {
        this(Bank.MAX_FAILED_LOGIN_ATTEMPTS);
    }

    public FailedPinAttemptRule(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public String getRuleName() {
        return "EXCESSIVE_FAILED_PIN";
    }

    @Override
    public Optional<SecurityAlert> evaluate(FraudContext context) {
        if (context.getFailedPinAttempts() >= maxAttempts) {
            String custId = context.getCustomer() != null ? context.getCustomer().getId() : "UNKNOWN";
            String desc = String.format("Account lockout triggered for %s after %d consecutive failed PIN attempts.",
                    custId, context.getFailedPinAttempts());
            SecurityAlert alert = new SecurityAlert(
                    IdGenerator.generateAlertId(),
                    custId,
                    context.getAccount() != null ? context.getAccount().getAccountId() : null,
                    AlertSeverity.CRITICAL,
                    getRuleName(),
                    desc
            );
            return Optional.of(alert);
        } else if (context.getFailedPinAttempts() >= 2) {
            String custId = context.getCustomer() != null ? context.getCustomer().getId() : "UNKNOWN";
            String desc = String.format("Multiple failed PIN attempts (%d) detected for %s.",
                    context.getFailedPinAttempts(), custId);
            SecurityAlert alert = new SecurityAlert(
                    IdGenerator.generateAlertId(),
                    custId,
                    context.getAccount() != null ? context.getAccount().getAccountId() : null,
                    AlertSeverity.MEDIUM,
                    getRuleName(),
                    desc
            );
            return Optional.of(alert);
        }
        return Optional.empty();
    }
}
