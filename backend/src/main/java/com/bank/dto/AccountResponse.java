package com.bank.dto;

import com.bank.enums.AccountStatus;
import com.bank.enums.AccountType;
import java.time.LocalDate;

public class AccountResponse {

    private Long id;
    private String accountId;
    private String customerId;
    private String customerName;
    private AccountType accountType;
    private double balance;
    private AccountStatus status;
    private double minimumBalance;
    private double interestRate;
    private double overdraftLimit;
    private Integer termMonths;
    private LocalDate maturityDate;
    private double estimatedInterest;
    private String lockReason;
    private LocalDate createdAt;

    public AccountResponse() {
    }

    public AccountResponse(Long id, String accountId, String customerId, String customerName,
                           AccountType accountType, double balance, AccountStatus status,
                           double minimumBalance, double interestRate, double overdraftLimit,
                           Integer termMonths, LocalDate maturityDate, double estimatedInterest,
                           String lockReason, LocalDate createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.minimumBalance = minimumBalance;
        this.interestRate = interestRate;
        this.overdraftLimit = overdraftLimit;
        this.termMonths = termMonths;
        this.maturityDate = maturityDate;
        this.estimatedInterest = estimatedInterest;
        this.lockReason = lockReason;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public double getEstimatedInterest() {
        return estimatedInterest;
    }

    public void setEstimatedInterest(double estimatedInterest) {
        this.estimatedInterest = estimatedInterest;
    }

    public String getLockReason() {
        return lockReason;
    }

    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}
