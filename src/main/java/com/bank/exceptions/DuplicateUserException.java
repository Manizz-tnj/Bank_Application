package com.bank.exceptions;

/**
 * Thrown when attempting to register a user with an identifier, email,
 * or phone number that already exists in the system.
 */
public class DuplicateUserException extends BankingException {

    private final String identifier;

    public DuplicateUserException(String message) {
        super(message);
        this.identifier = null;
    }

    public DuplicateUserException(String message, String identifier) {
        super(message);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
