package com.bank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public class TransferRequest {

    @NotBlank(message = "Source account ID is required")
    private String fromAccountId;

    @NotBlank(message = "Destination account ID is required")
    private String toAccountId;

    @DecimalMin(value = "0.01", message = "Transfer amount must be strictly greater than 0")
    private double amount;

    private String description;
    private String idempotencyKey;

    public TransferRequest() {
    }

    public TransferRequest(String fromAccountId, String toAccountId, double amount, String description) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.description = description;
    }

    public TransferRequest(String fromAccountId, String toAccountId, double amount, String description, String idempotencyKey) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
