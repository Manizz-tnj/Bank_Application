package com.bank.exceptions;

/**
 * Thrown when an account does not have sufficient funds (or overdraft limit)
 * to satisfy a withdrawal or debit transfer.
 */
public class InsufficientBalanceException extends BankingException {

    private final double requestedAmount;
    private final double availableBalance;

    public InsufficientBalanceException(String message) {
        super(message);
        this.requestedAmount = 0.0;
        this.availableBalance = 0.0;
    }

    public InsufficientBalanceException(String message, double requestedAmount, double availableBalance) {
        super(message);
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }
}
