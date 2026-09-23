package com.bank.exceptions;

/**
 * Thrown when a transaction amount is zero, negative, or fails numerical limits.
 */
public class InvalidAmountException extends BankingException {

    private final double amount;

    public InvalidAmountException(String message) {
        super(message);
        this.amount = 0.0;
    }

    public InvalidAmountException(String message, double amount) {
        super(message);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
