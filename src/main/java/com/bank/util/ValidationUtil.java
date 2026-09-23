package com.bank.util;

import com.bank.exceptions.BankingException;
import com.bank.exceptions.InvalidAmountException;

import java.util.regex.Pattern;

/**
 * Defensive input validation utilities ensuring system integrity.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern PIN_PATTERN =
            Pattern.compile("^[0-9]{4,6}$");

    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void validateNonEmpty(String input, String fieldName) throws BankingException {
        if (input == null || input.trim().isEmpty()) {
            throw new BankingException(fieldName + " cannot be blank or empty.");
        }
    }

    public static void validateEmail(String email) throws BankingException {
        validateNonEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new BankingException("Invalid email format. Example: user@domain.com");
        }
    }

    public static void validatePhone(String phone) throws BankingException {
        validateNonEmpty(phone, "Phone");
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new BankingException("Invalid phone number format. Must contain 7 to 15 digits.");
        }
    }

    public static void validatePinFormat(String pin) throws BankingException {
        validateNonEmpty(pin, "PIN");
        if (!PIN_PATTERN.matcher(pin.trim()).matches()) {
            throw new BankingException("PIN must be between 4 and 6 numeric digits.");
        }
    }

    public static void validatePositiveAmount(double amount, String context) throws InvalidAmountException {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0.0) {
            throw new InvalidAmountException(context + " amount must be strictly positive and valid.", amount);
        }
    }
}
