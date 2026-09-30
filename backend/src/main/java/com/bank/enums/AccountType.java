package com.bank.enums;

public enum AccountType {
    SAVINGS("Savings Account"),
    CURRENT("Current Account"),
    FIXED_DEPOSIT("Fixed Deposit Account");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
