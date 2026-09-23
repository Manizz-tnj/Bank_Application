package com.bank.exceptions;

/**
 * Thrown when attempting to register an account with an existing account ID.
 */
public class DuplicateAccountException extends BankingException {

    private final String accountId;

    public DuplicateAccountException(String message) {
        super(message);
        this.accountId = null;
    }

    public DuplicateAccountException(String message, String accountId) {
        super(message);
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
