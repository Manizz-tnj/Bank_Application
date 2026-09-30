package com.bank.exception;

public class DuplicateUserException extends BankingException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
