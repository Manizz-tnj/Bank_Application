package com.bank.enums;

public enum UserRole {
    CUSTOMER("Customer"),
    EMPLOYEE("Bank Employee"),
    ADMIN("System Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
