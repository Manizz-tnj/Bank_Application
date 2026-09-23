package com.bank.security;

import com.bank.model.SecurityAlert;

import java.util.Optional;

/**
 * Strategy interface for rule-based suspicious activity and fraud detection.
 * Demonstrates Abstraction and Strategy Pattern.
 */
public interface FraudRule {

    /**
     * Unique identifier/name of the fraud rule.
     */
    String getRuleName();

    /**
     * Evaluates the contextual transaction or access event.
     * @param context Fraud context data
     * @return Optional containing SecurityAlert if violated, or Optional.empty()
     */
    Optional<SecurityAlert> evaluate(FraudContext context);
}
