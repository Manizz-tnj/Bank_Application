package com.bank.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Utility for secure Core Java cryptographic hashing without external libraries.
 * Uses SHA-256 with cryptographically strong randomized salts.
 */
public final class HashUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String ALGORITHM = "SHA-256";

    // Private constructor to prevent instantiation of utility class
    private HashUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generates a random 16-byte cryptographic salt formatted as a hex string.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    /**
     * Computes the SHA-256 digest of the salt prepended to the raw PIN.
     */
    public static String hashPin(String rawPin, String salt) {
        if (rawPin == null || salt == null) {
            throw new IllegalArgumentException("PIN and salt must not be null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = md.digest(rawPin.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available on JVM runtime", e);
        }
    }

    /**
     * Verifies if a raw PIN matches the expected hash using constant-time comparison.
     */
    public static boolean verifyPin(String rawPin, String salt, String expectedHash) {
        if (rawPin == null || salt == null || expectedHash == null) {
            return false;
        }
        String computedHash = hashPin(rawPin, salt);
        return MessageDigest.isEqual(computedHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
