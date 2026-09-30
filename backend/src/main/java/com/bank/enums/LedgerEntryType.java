package com.bank.enums;

public enum LedgerEntryType {
    DEBIT("Debit - Outflow / Deduction"),
    CREDIT("Credit - Inflow / Addition");

    private final String description;

    LedgerEntryType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
