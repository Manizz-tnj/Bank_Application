package com.bank.model;

import com.bank.enums.AccountStatus;
import com.bank.enums.AccountType;
import com.bank.exceptions.AccountClosedException;
import com.bank.exceptions.AccountLockedException;
import com.bank.exceptions.BankingException;
import com.bank.exceptions.InvalidAmountException;
import com.bank.interfaces.TransactionOperations;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract base class representing a bank account.
 * Implements Abstraction, Encapsulation, and TransactionOperations contract.
 */
public abstract class Account implements TransactionOperations, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Encapsulated fields
    private String accountId;
    private String customerId;
    private AccountType accountType;
    private double balance;
    private AccountStatus status;
    private LocalDate createdDate;
    private String lockReason;

    /**
     * Default constructor for serialization support.
     */
    protected Account() {
        this("", "", AccountType.SAVINGS, 0.0);
    }

    /**
     * Base parameterized constructor demonstrating constructor chaining.
     */
    protected Account(String accountId, String customerId, AccountType accountType, double initialBalance) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = initialBalance;
        this.status = AccountStatus.ACTIVE;
        this.createdDate = LocalDate.now();
        this.lockReason = null;
    }

    // Abstract methods to be overridden by subclasses
    public abstract double calculateInterest();

    public abstract void displayAccountDetails();

    // Verification guards
    protected void ensureAccountIsOperational() throws BankingException {
        if (status == AccountStatus.CLOSED) {
            throw new AccountClosedException("Account " + accountId + " is CLOSED. Operations not permitted.", accountId);
        }
        if (status == AccountStatus.LOCKED) {
            throw new AccountLockedException("Account " + accountId + " is LOCKED: " + lockReason, accountId);
        }
    }

    // Method Overloading: deposit(amount) delegates to deposit(amount, description)
    @Override
    public void deposit(double amount) throws BankingException {
        deposit(amount, "Deposit to " + accountId);
    }

    @Override
    public void deposit(double amount, String description) throws BankingException {
        ensureAccountIsOperational();
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be strictly greater than 0.", amount);
        }
        this.balance += amount;
    }

    // Method Overloading: withdraw(amount) delegates to withdraw(amount, description)
    @Override
    public void withdraw(double amount) throws BankingException {
        withdraw(amount, "Withdrawal from " + accountId);
    }

    // Withdraw implementation delegated to subclasses for specific balance & overdraft rules
    @Override
    public abstract void withdraw(double amount, String description) throws BankingException;

    // Direct balance credit/debit for atomic transfer operations
    public void credit(double amount) throws BankingException {
        deposit(amount, "Transfer credit");
    }

    public void debit(double amount) throws BankingException {
        withdraw(amount, "Transfer debit");
    }

    // Controlled status modifications
    public void lockAccount(String reason) {
        this.status = AccountStatus.LOCKED;
        this.lockReason = reason;
    }

    public void unlockAccount() {
        this.status = AccountStatus.ACTIVE;
        this.lockReason = null;
    }

    public void closeAccount() throws BankingException {
        if (this.balance > 0.01) {
            throw new BankingException("Account " + accountId + " cannot be closed with remaining balance of " + balance + ". Withdraw funds first.");
        }
        this.status = AccountStatus.CLOSED;
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    public boolean isLocked() {
        return this.status == AccountStatus.LOCKED;
    }

    public boolean isClosed() {
        return this.status == AccountStatus.CLOSED;
    }

    // Getters and protected setters
    public String getAccountId() {
        return accountId;
    }

    protected void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    protected void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    protected void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    protected void setBalance(double balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getLockReason() {
        return lockReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return Objects.equals(accountId, account.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public String toString() {
        return String.format("[%s] ACC: %s | Customer: %s | Balance: $%,.2f | Status: %s",
                accountType, accountId, customerId, balance, status);
    }
}
