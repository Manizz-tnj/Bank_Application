package com.bank.security;

import com.bank.model.SecurityAlert;
import com.bank.repository.SecurityAlertRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Fraud Detection Engine executing modular fraud rules against banking activities.
 * Demonstrates Strategy Pattern, Polymorphism, and Separation of Concerns.
 */
public class FraudDetectionService {

    private final SecurityAlertRepository alertRepository;
    private final List<FraudRule> rules;

    public FraudDetectionService(SecurityAlertRepository alertRepository) {
        this.alertRepository = Objects.requireNonNull(alertRepository, "Alert repository must not be null");
        this.rules = new ArrayList<>();
        registerDefaultRules();
    }

    private void registerDefaultRules() {
        rules.add(new LargeTransactionRule());
        rules.add(new FailedPinAttemptRule());
        rules.add(new TransferVelocityRule());
        rules.add(new UnusualActivityRule());
    }

    public void addRule(FraudRule rule) {
        if (rule != null) {
            rules.add(rule);
        }
    }

    /**
     * Evaluates all registered fraud rules against the provided context.
     * Persists any triggered alerts and returns them.
     */
    public List<SecurityAlert> evaluate(FraudContext context) {
        List<SecurityAlert> triggeredAlerts = new ArrayList<>();

        for (FraudRule rule : rules) {
            Optional<SecurityAlert> alertOpt = rule.evaluate(context);
            if (alertOpt.isPresent()) {
                SecurityAlert alert = alertOpt.get();
                alertRepository.save(alert);
                triggeredAlerts.add(alert);
                System.out.println("! [SECURITY AUDIT] Rule triggered: " + alert.getRuleTriggered() + " - " + alert.getDescription());
            }
        }

        return triggeredAlerts;
    }
}
