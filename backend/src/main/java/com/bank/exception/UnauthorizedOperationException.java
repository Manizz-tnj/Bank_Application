package com.bank.exception;

public class UnauthorizedOperationException extends BankingException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
