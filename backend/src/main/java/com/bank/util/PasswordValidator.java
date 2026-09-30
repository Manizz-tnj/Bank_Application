package com.bank.util;

import java.util.regex.Pattern;

public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    private PasswordValidator() {
        // Utility class
    }

    public static void validatePassword(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long.");
        }
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
        }
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter.");
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one digit.");
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one special character.");
        }
    }

    public static void validatePin(String pin) {
        if (pin == null || !pin.matches("^\\d{4,6}$")) {
            throw new IllegalArgumentException("Transaction PIN must be a 4 to 6 digit numeric code.");
        }

        // Check for all identical digits (e.g., 0000, 1111)
        boolean allSame = true;
        for (int i = 1; i < pin.length(); i++) {
            if (pin.charAt(i) != pin.charAt(0)) {
                allSame = false;
                break;
            }
        }
        if (allSame) {
            throw new IllegalArgumentException("Transaction PIN cannot contain repeating identical digits (e.g., " + pin + ").");
        }

        // Check for sequential ascending (e.g., 1234, 123456)
        boolean ascending = true;
        for (int i = 1; i < pin.length(); i++) {
            if (pin.charAt(i) - pin.charAt(i - 1) != 1) {
                ascending = false;
                break;
            }
        }
        if (ascending) {
            throw new IllegalArgumentException("Transaction PIN cannot be sequential ascending digits.");
        }

        // Check for sequential descending (e.g., 4321, 654321)
        boolean descending = true;
        for (int i = 1; i < pin.length(); i++) {
            if (pin.charAt(i - 1) - pin.charAt(i) != 1) {
                descending = false;
                break;
            }
        }
        if (descending) {
            throw new IllegalArgumentException("Transaction PIN cannot be sequential descending digits.");
        }
    }
}
