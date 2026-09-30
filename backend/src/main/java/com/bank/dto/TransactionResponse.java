package com.bank.dto;

import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String transactionId;
    private String accountId;
    private TransactionType transactionType;
    private double amount;
    private TransactionStatus status;
    private LocalDateTime timestamp;
    private String description;
    private String referenceAccountId;

    public TransactionResponse() {
    }

    public TransactionResponse(Long id, String transactionId, String accountId,
                               TransactionType transactionType, double amount,
                               TransactionStatus status, LocalDateTime timestamp,
                               String description, String referenceAccountId) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
        this.description = description;
        this.referenceAccountId = referenceAccountId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceAccountId() {
        return referenceAccountId;
    }

    public void setReferenceAccountId(String referenceAccountId) {
        this.referenceAccountId = referenceAccountId;
    }
}
