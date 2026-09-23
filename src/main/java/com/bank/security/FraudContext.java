package com.bank.security;

import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.Transaction;

import java.util.Collections;
import java.util.List;

/**
 * Context object holding runtime information evaluated by Fraud Rules.
 * Demonstrates Encapsulation and Builder/Parameter object patterns.
 */
public class FraudContext {

    private final Customer customer;
    private final Account account;
    private final Transaction transaction;
    private final int failedPinAttempts;
    private final List<Transaction> recentTransfers;
    private final String description;

    public FraudContext(Customer customer, Account account, Transaction transaction,
                        int failedPinAttempts, List<Transaction> recentTransfers, String description) {
        this.customer = customer;
        this.account = account;
        this.transaction = transaction;
        this.failedPinAttempts = failedPinAttempts;
        this.recentTransfers = recentTransfers != null ? recentTransfers : Collections.emptyList();
        this.description = description;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Account getAccount() {
        return account;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public int getFailedPinAttempts() {
        return failedPinAttempts;
    }

    public List<Transaction> getRecentTransfers() {
        return Collections.unmodifiableList(recentTransfers);
    }

    public String getDescription() {
        return description;
    }
}
