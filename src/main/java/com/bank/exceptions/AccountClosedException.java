package com.bank.exceptions;

/**
 * Thrown when attempting any transaction or modification on an account
 * that has been closed.
 */
public class AccountClosedException extends BankingException {

    private final String accountId;

    public AccountClosedException(String message) {
        super(message);
        this.accountId = null;
    }

    public AccountClosedException(String message, String accountId) {
        super(message);
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
