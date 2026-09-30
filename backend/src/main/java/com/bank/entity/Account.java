package com.bank.entity;

import com.bank.enums.AccountStatus;
import com.bank.enums.AccountType;
import com.bank.exception.AccountClosedException;
import com.bank.exception.AccountLockedException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidAmountException;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", unique = true, nullable = false, length = 30)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(nullable = false)
    private double balance = 0.0;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "minimum_balance", nullable = false)
    private double minimumBalance = 0.0;

    @Column(name = "interest_rate", nullable = false)
    private double interestRate = 0.0;

    @Column(name = "overdraft_limit", nullable = false)
    private double overdraftLimit = 0.0;

    @Column(name = "term_months")
    private Integer termMonths;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "lock_reason", length = 255)
    private String lockReason;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    public Account() {
    }

    public Account(String accountId, Customer customer, AccountType accountType, double initialBalance) {
        this.accountId = accountId;
        this.customer = customer;
        this.accountType = accountType;
        this.balance = initialBalance;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = LocalDate.now();

        if (accountType == AccountType.SAVINGS) {
            this.minimumBalance = 1000.0;
            this.interestRate = 4.0;
        } else if (accountType == AccountType.CURRENT) {
            this.overdraftLimit = 10000.0;
            this.interestRate = 0.0;
        } else if (accountType == AccountType.FIXED_DEPOSIT) {
            this.interestRate = 7.0;
            this.termMonths = 12;
            this.maturityDate = LocalDate.now().plusMonths(12);
        }
    }

    public void addTransaction(Transaction txn) {
        transactions.add(txn);
        txn.setAccount(this);
    }

    public void deposit(double amount) {
        ensureOperational();
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive", amount);
        }
        if (accountType == AccountType.FIXED_DEPOSIT) {
            throw new InvalidAmountException("Fixed Deposit accounts do not accept top-up deposits", amount);
        }
        this.balance += amount;
    }

    public void withdraw(double amount) {
        ensureOperational();
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive", amount);
        }

        if (accountType == AccountType.SAVINGS) {
            double remaining = this.balance - amount;
            if (remaining < minimumBalance) {
                throw new InsufficientBalanceException(
                        String.format("Withdrawal denied: Maintains balance ($%,.2f) below required minimum ($%,.2f)",
                                remaining, minimumBalance),
                        amount, this.balance - minimumBalance);
            }
            this.balance = remaining;
        } else if (accountType == AccountType.CURRENT) {
            double totalAvailable = this.balance + overdraftLimit;
            if (amount > totalAvailable) {
                throw new InsufficientBalanceException(
                        String.format("Overdraft exceeded! Requested: $%,.2f, Total Available: $%,.2f",
                                amount, totalAvailable),
                        amount, totalAvailable);
            }
            this.balance -= amount;
        } else if (accountType == AccountType.FIXED_DEPOSIT) {
            if (amount > this.balance) {
                throw new InsufficientBalanceException("Cannot withdraw more than current FD balance", amount, this.balance);
            }
            this.balance -= amount;
        }
    }

    public double calculateInterest() {
        if (accountType == AccountType.SAVINGS) {
            return (balance * interestRate) / 100.0;
        } else if (accountType == AccountType.FIXED_DEPOSIT && termMonths != null) {
            return (balance * interestRate * termMonths) / (12.0 * 100.0);
        }
        return 0.0;
    }

    public void ensureOperational() {
        if (status == AccountStatus.CLOSED) {
            throw new AccountClosedException("Account " + accountId + " is CLOSED", accountId);
        }
        if (status == AccountStatus.LOCKED) {
            throw new AccountLockedException("Account " + accountId + " is LOCKED: " + lockReason, accountId);
        }
    }

    // Getters and Setters
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
