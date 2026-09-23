package com.bank.exceptions;

/**
 * Thrown when a user attempts an operation without having the necessary
 * role or customer ownership permissions.
 */
public class UnauthorizedOperationException extends BankingException {

    private final String userId;
    private final String operation;

    public UnauthorizedOperationException(String message) {
        super(message);
        this.userId = null;
        this.operation = null;
    }

    public UnauthorizedOperationException(String message, String userId, String operation) {
        super(message);
        this.userId = userId;
        this.operation = operation;
    }

    public String getUserId() {
        return userId;
    }

    public String getOperation() {
        return operation;
    }
}
