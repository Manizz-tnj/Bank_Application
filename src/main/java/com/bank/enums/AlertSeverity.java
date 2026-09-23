package com.bank.enums;

/**
 * Severity levels for banking security and fraud alerts.
 */
public enum AlertSeverity {
    LOW("Low Severity - Routine audit event"),
    MEDIUM("Medium Severity - Suspicious anomaly observed"),
    HIGH("High Severity - Immediate review required"),
    CRITICAL("Critical Severity - Action taken, account potentially compromised");

    private final String description;

    AlertSeverity(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
