package com.bank.exceptions;

/**
 * Thrown when a customer ID or profile cannot be located in the system.
 */
public class CustomerNotFoundException extends BankingException {

    private final String customerId;

    public CustomerNotFoundException(String message) {
        super(message);
        this.customerId = null;
    }

    public CustomerNotFoundException(String message, String customerId) {
        super(message);
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }
}
