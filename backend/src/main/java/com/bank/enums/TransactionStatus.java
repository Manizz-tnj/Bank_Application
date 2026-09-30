package com.bank.enums;

public enum TransactionStatus {
    SUCCESS("Transaction Completed"),
    FAILED("Transaction Failed");

    private final String message;

    TransactionStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
