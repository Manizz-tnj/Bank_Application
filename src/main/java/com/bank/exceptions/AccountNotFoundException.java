package com.bank.exceptions;

/**
 * Thrown when an account ID cannot be located in the system.
 */
public class AccountNotFoundException extends BankingException {

    private final String accountId;

    public AccountNotFoundException(String message) {
        super(message);
        this.accountId = null;
    }

    public AccountNotFoundException(String message, String accountId) {
        super(message);
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
