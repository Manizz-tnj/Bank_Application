package com.bank.exception;

public class InsufficientBalanceException extends BankingException {
    private final double requestedAmount;
    private final double availableBalance;

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
