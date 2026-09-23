package com.bank.exceptions;

/**
 * Base checked exception for all domain-specific banking errors.
 */
public class BankingException extends Exception {

    public BankingException(String message) {
        super(message);
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
}
