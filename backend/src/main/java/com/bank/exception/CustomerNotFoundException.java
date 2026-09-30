package com.bank.exception;

public class CustomerNotFoundException extends BankingException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
