package com.bank.exception;

public class AccountClosedException extends BankingException {
    public AccountClosedException(String message, String accountId) {
        super(message);
    }
}
