package com.bank.enums;

public enum AccountStatus {
    ACTIVE("Active"),
    LOCKED("Locked due to security violations"),
    CLOSED("Closed");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
