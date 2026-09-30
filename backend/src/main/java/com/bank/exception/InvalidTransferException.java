package com.bank.exception;

public class InvalidTransferException extends BankingException {
    public InvalidTransferException(String message) {
        super(message);
    }
}
