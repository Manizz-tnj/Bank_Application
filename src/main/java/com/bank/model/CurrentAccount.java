package com.bank.model;

import com.bank.enums.AccountType;
import com.bank.exceptions.BankingException;
import com.bank.exceptions.InsufficientBalanceException;
import com.bank.exceptions.InvalidAmountException;

import java.io.Serial;

/**
 * Current Account offering overdraft facility for business and commercial transactions.
 * Demonstrates Inheritance, Method Overriding, and Polymorphism.
 */
public class CurrentAccount extends Account {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final double DEFAULT_OVERDRAFT_LIMIT = 10000.0;

    private final double overdraftLimit;

    public CurrentAccount() {
        this("", "", 0.0);
    }

    public CurrentAccount(String accountId, String customerId, double initialBalance) {
        this(accountId, customerId, initialBalance, DEFAULT_OVERDRAFT_LIMIT);
    }

    public CurrentAccount(String accountId, String customerId, double initialBalance, double overdraftLimit) {
        super(accountId, customerId, AccountType.CURRENT, initialBalance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public double calculateInterest() {
        // Current accounts do not earn positive credit interest
        return 0.0;
    }

    @Override
    public void withdraw(double amount, String description) throws BankingException {
        ensureAccountIsOperational();
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly greater than 0.", amount);
        }
        double availableFunds = getBalance() + overdraftLimit;
        if (amount > availableFunds) {
            throw new InsufficientBalanceException(
                    String.format("Overdraft limit exceeded! Requested: $%,.2f, Total Available (Balance + Overdraft): $%,.2f",
                            amount, availableFunds),
                    amount, availableFunds);
        }
        setBalance(getBalance() - amount);
    }

    @Override
    public void displayAccountDetails() {
        System.out.println("==================================================");
        System.out.println(" CURRENT ACCOUNT DETAILS");
        System.out.println("==================================================");
        System.out.println("Account ID       : " + getAccountId());
        System.out.println("Customer ID      : " + getCustomerId());
        System.out.println("Account Type     : " + getAccountType().getDescription());
        System.out.println("Current Balance  : $" + String.format("%,.2f", getBalance()));
        System.out.println("Overdraft Limit  : $" + String.format("%,.2f", overdraftLimit));
        System.out.println("Available Limit  : $" + String.format("%,.2f", getBalance() + overdraftLimit));
        System.out.println("Account Status   : " + getStatus());
        System.out.println("Created Date     : " + getCreatedDate());
        System.out.println("==================================================");
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }
}
