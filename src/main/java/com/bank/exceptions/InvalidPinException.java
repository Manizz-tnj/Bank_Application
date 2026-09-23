package com.bank.exceptions;

/**
 * Thrown when an invalid or incorrect PIN is provided during authentication
 * or security-sensitive operations.
 */
public class InvalidPinException extends BankingException {

    private final int attemptsRemaining;

    public InvalidPinException(String message) {
        super(message);
        this.attemptsRemaining = -1;
    }

    public InvalidPinException(String message, int attemptsRemaining) {
        super(message);
        this.attemptsRemaining = attemptsRemaining;
    }

    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }
}
