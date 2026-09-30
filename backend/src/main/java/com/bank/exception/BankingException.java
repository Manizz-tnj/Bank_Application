package com.bank.exception;

public class BankingException extends RuntimeException {
    public BankingException(String message) {
        super(message);
    }
}
