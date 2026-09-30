package com.bank.dto;

import jakarta.validation.constraints.DecimalMin;

public class DepositWithdrawRequest {

    @DecimalMin(value = "0.01", message = "Amount must be strictly greater than 0")
    private double amount;

    private String description;

    public DepositWithdrawRequest() {
    }

    public DepositWithdrawRequest(double amount, String description) {
        this.amount = amount;
        this.description = description;
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
}
