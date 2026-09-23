package com.bank.enums;

/**
 * Lifecycle status of a security alert.
 */
public enum AlertStatus {
    OPEN("Open / Unreviewed"),
    INVESTIGATING("Under Investigation"),
    RESOLVED("Resolved / Closed"),
    DISMISSED("False Alarm / Dismissed");

    private final String label;

    AlertStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
