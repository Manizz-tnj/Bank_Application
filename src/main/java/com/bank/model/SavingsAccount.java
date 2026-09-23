package com.bank.model;

import com.bank.enums.AccountType;
import com.bank.exceptions.BankingException;
import com.bank.exceptions.InsufficientBalanceException;
import com.bank.exceptions.InvalidAmountException;

import java.io.Serial;

/**
 * Savings Account requiring a minimum balance and earning annual interest.
 * Demonstrates Inheritance, Method Overriding, and Polymorphism.
 */
public class SavingsAccount extends Account {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final double DEFAULT_MINIMUM_BALANCE = 1000.0;
    public static final double DEFAULT_INTEREST_RATE = 4.0; // 4% per annum

    private final double minimumBalance;
    private final double interestRate;

    public SavingsAccount() {
        this("", "", 0.0);
    }

    public SavingsAccount(String accountId, String customerId, double initialBalance) {
        this(accountId, customerId, initialBalance, DEFAULT_MINIMUM_BALANCE, DEFAULT_INTEREST_RATE);
    }

    public SavingsAccount(String accountId, String customerId, double initialBalance,
                          double minimumBalance, double interestRate) {
        super(accountId, customerId, AccountType.SAVINGS, initialBalance);
        this.minimumBalance = minimumBalance;
        this.interestRate = interestRate;
    }

    @Override
    public double calculateInterest() {
        return (getBalance() * interestRate) / 100.0;
    }

    @Override
    public void withdraw(double amount, String description) throws BankingException {
        ensureAccountIsOperational();
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly greater than 0.", amount);
        }
        double effectiveBalance = getBalance() - amount;
        if (effectiveBalance < minimumBalance) {
            throw new InsufficientBalanceException(
                    String.format("Withdrawal denied: Maintains balance ($%,.2f) below required minimum balance ($%,.2f).",
                            effectiveBalance, minimumBalance),
                    amount, getBalance() - minimumBalance);
        }
        setBalance(effectiveBalance);
    }

    @Override
    public void displayAccountDetails() {
        System.out.println("==================================================");
        System.out.println(" SAVINGS ACCOUNT DETAILS");
        System.out.println("==================================================");
        System.out.println("Account ID       : " + getAccountId());
        System.out.println("Customer ID      : " + getCustomerId());
        System.out.println("Account Type     : " + getAccountType().getDescription());
        System.out.println("Current Balance  : $" + String.format("%,.2f", getBalance()));
        System.out.println("Minimum Balance  : $" + String.format("%,.2f", minimumBalance));
        System.out.println("Interest Rate    : " + interestRate + "% p.a.");
        System.out.println("Annual Interest  : $" + String.format("%,.2f", calculateInterest()));
        System.out.println("Account Status   : " + getStatus());
        System.out.println("Created Date     : " + getCreatedDate());
        System.out.println("==================================================");
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public double getInterestRate() {
        return interestRate;
    }
}
