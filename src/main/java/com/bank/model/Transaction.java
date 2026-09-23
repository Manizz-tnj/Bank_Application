package com.bank.model;

import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents an immutable financial transaction audit record.
 * Implements Encapsulation and Serialization.
 */
public class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String transactionId;
    private final String accountId;
    private final TransactionType transactionType;
    private final double amount;
    private final LocalDateTime dateTime;
    private final String description;
    private final TransactionStatus status;
    private final String referenceId; // Destination account or counterpart reference

    /**
     * Default constructor for serialization.
     */
    public Transaction() {
        this("", "", TransactionType.DEPOSIT, 0.0, "", TransactionStatus.SUCCESS, null);
    }

    /**
     * Base constructor defaulting to current timestamp.
     */
    public Transaction(String transactionId, String accountId, TransactionType transactionType,
                       double amount, String description, TransactionStatus status, String referenceId) {
        this(transactionId, accountId, transactionType, amount, LocalDateTime.now(), description, status, referenceId);
    }

    /**
     * Full constructor with explicit timestamp.
     */
    public Transaction(String transactionId, String accountId, TransactionType transactionType,
                       double amount, LocalDateTime dateTime, String description,
                       TransactionStatus status, String referenceId) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.dateTime = dateTime != null ? dateTime : LocalDateTime.now();
        this.description = description;
        this.status = status;
        this.referenceId = referenceId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getDescription() {
        return description;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getFormattedDateTime() {
        return dateTime.format(FORMATTER);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Acc: %s | %-10s | Amount: $%,.2f | Status: %s | Ref: %s",
                getFormattedDateTime(), transactionId, accountId, transactionType, amount, status,
                referenceId != null ? referenceId : "N/A");
    }
}
