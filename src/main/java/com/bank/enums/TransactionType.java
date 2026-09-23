package com.bank.enums;

/**
 * Types of financial transactions.
 */
public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER("Transfer");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
