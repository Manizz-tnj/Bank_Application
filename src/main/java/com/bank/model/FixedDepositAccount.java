package com.bank.model;

import com.bank.enums.AccountType;
import com.bank.exceptions.BankingException;
import com.bank.exceptions.InsufficientBalanceException;
import com.bank.exceptions.InvalidAmountException;

import java.io.Serial;
import java.time.LocalDate;

/**
 * Fixed Deposit Account offering higher interest over a predetermined maturity period.
 * Restricts intermediate top-ups and premature withdrawals.
 * Demonstrates Inheritance, Method Overriding, and Polymorphism.
 */
public class FixedDepositAccount extends Account {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final double DEFAULT_FD_INTEREST_RATE = 7.0; // 7% p.a.
    public static final double PREMATURE_PENALTY_PERCENTAGE = 1.5; // 1.5% penalty on interest

    private final int termMonths;
    private final double interestRate;
    private final LocalDate maturityDate;
    private final double principalAmount;

    public FixedDepositAccount() {
        this("", "", 0.0, 12);
    }

    public FixedDepositAccount(String accountId, String customerId, double depositAmount, int termMonths) {
        this(accountId, customerId, depositAmount, termMonths, DEFAULT_FD_INTEREST_RATE);
    }

    public FixedDepositAccount(String accountId, String customerId, double depositAmount,
                               int termMonths, double interestRate) {
        super(accountId, customerId, AccountType.FIXED_DEPOSIT, depositAmount);
        this.principalAmount = depositAmount;
        this.termMonths = termMonths;
        this.interestRate = interestRate;
        this.maturityDate = getCreatedDate().plusMonths(termMonths);
    }

    @Override
    public double calculateInterest() {
        // Simple fixed deposit term interest formula: (P * R * T) / (12 * 100)
        return (principalAmount * interestRate * termMonths) / (12.0 * 100.0);
    }

    public double getMaturityAmount() {
        return principalAmount + calculateInterest();
    }

    public boolean isMatured() {
        return !LocalDate.now().isBefore(maturityDate);
    }

    @Override
    public void deposit(double amount, String description) throws BankingException {
        throw new BankingException("Fixed Deposit accounts do not accept recurring or top-up deposits. Please open a new FD account.");
    }

    @Override
    public void withdraw(double amount, String description) throws BankingException {
        ensureAccountIsOperational();
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly greater than 0.", amount);
        }
        if (amount > getBalance()) {
            throw new InsufficientBalanceException("Cannot withdraw more than current FD balance.", amount, getBalance());
        }

        // Premature withdrawal check
        if (!isMatured()) {
            System.out.printf("! NOTICE: Premature FD withdrawal before maturity (%s). A penalty of %.1f%% applies.%n",
                    maturityDate, PREMATURE_PENALTY_PERCENTAGE);
        }

        setBalance(getBalance() - amount);
    }

    @Override
    public void displayAccountDetails() {
        System.out.println("==================================================");
        System.out.println(" FIXED DEPOSIT ACCOUNT DETAILS");
        System.out.println("==================================================");
        System.out.println("Account ID       : " + getAccountId());
        System.out.println("Customer ID      : " + getCustomerId());
        System.out.println("Account Type     : " + getAccountType().getDescription());
        System.out.println("Principal Amount : $" + String.format("%,.2f", principalAmount));
        System.out.println("Current Balance  : $" + String.format("%,.2f", getBalance()));
        System.out.println("Deposit Term     : " + termMonths + " Months");
        System.out.println("Interest Rate    : " + interestRate + "% p.a.");
        System.out.println("Maturity Date    : " + maturityDate);
        System.out.println("Maturity Status  : " + (isMatured() ? "MATURED (Ready for Payout)" : "ACTIVE"));
        System.out.println("Estimated Total  : $" + String.format("%,.2f", getMaturityAmount()));
        System.out.println("Account Status   : " + getStatus());
        System.out.println("==================================================");
    }

    public int getTermMonths() {
        return termMonths;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }
}
