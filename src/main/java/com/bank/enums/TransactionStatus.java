package com.bank.enums;

/**
 * Status of a financial transaction execution.
 */
public enum TransactionStatus {
    SUCCESS("Transaction Completed"),
    FAILED("Transaction Failed"),
    REVERSED("Transaction Reversed");

    private final String message;

    TransactionStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
