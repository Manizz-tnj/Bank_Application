package com.bank.exceptions;

/**
 * Thrown when an operation is attempted on an account that has been locked
 * due to security violations or excessive failed login attempts.
 */
public class AccountLockedException extends BankingException {

    private final String identifier;

    public AccountLockedException(String message) {
        super(message);
        this.identifier = null;
    }

    public AccountLockedException(String message, String identifier) {
        super(message);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
