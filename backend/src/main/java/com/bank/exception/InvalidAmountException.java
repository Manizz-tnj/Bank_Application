package com.bank.exception;

public class InvalidAmountException extends BankingException {
    private final double amount;

    public InvalidAmountException(String message, double amount) {
        super(message);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
