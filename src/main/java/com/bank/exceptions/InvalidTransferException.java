package com.bank.exceptions;

/**
 * Thrown when a fund transfer request is invalid (e.g. transfer to same account,
 * invalid destination, or transfer between incompatible statuses).
 */
public class InvalidTransferException extends BankingException {

    public InvalidTransferException(String message) {
        super(message);
    }

    public InvalidTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}
