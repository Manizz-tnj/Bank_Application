package com.bank.exception;

public class AccountLockedException extends BankingException {
    public AccountLockedException(String message) {
        super(message);
    }

    public AccountLockedException(String message, String identifier) {
        super(message);
    }
}
