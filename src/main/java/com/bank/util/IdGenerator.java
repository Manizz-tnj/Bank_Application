package com.bank.util;

import com.bank.enums.AccountType;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe identifier generation utility for banking entities.
 * Generates structured, readable, and sequential unique identifiers.
 */
public final class IdGenerator {

    private static final AtomicLong CUSTOMER_SEQ = new AtomicLong(1000);
    private static final AtomicLong EMPLOYEE_SEQ = new AtomicLong(2000);
    private static final AtomicLong ADMIN_SEQ = new AtomicLong(100);
    private static final AtomicLong ACCOUNT_SEQ = new AtomicLong(5000);
    private static final AtomicLong TXN_SEQ = new AtomicLong(10000);
    private static final AtomicLong ALERT_SEQ = new AtomicLong(100);

    private IdGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String generateCustomerId() {
        return "CUST-" + CUSTOMER_SEQ.incrementAndGet();
    }

    public static String generateEmployeeId() {
        return "EMP-" + EMPLOYEE_SEQ.incrementAndGet();
    }

    public static String generateAdminId() {
        return "ADM-" + ADMIN_SEQ.incrementAndGet();
    }

    public static String generateAccountId(AccountType type) {
        String prefix = switch (type) {
            case SAVINGS -> "ACC-SAV-";
            case CURRENT -> "ACC-CUR-";
            case FIXED_DEPOSIT -> "ACC-FD-";
        };
        return prefix + ACCOUNT_SEQ.incrementAndGet();
    }

    public static String generateTransactionId() {
        return "TXN-" + TXN_SEQ.incrementAndGet();
    }

    public static String generateAlertId() {
        return "ALT-" + ALERT_SEQ.incrementAndGet();
    }

    /**
     * Updates internal counter if an existing loaded ID is higher.
     */
    public static void synchronizeCustomerSeq(long highest) {
        CUSTOMER_SEQ.updateAndGet(current -> Math.max(current, highest));
    }

    public static void synchronizeEmployeeSeq(long highest) {
        EMPLOYEE_SEQ.updateAndGet(current -> Math.max(current, highest));
    }

    public static void synchronizeAccountSeq(long highest) {
        ACCOUNT_SEQ.updateAndGet(current -> Math.max(current, highest));
    }

    public static void synchronizeTxnSeq(long highest) {
        TXN_SEQ.updateAndGet(current -> Math.max(current, highest));
    }

    public static void synchronizeAlertSeq(long highest) {
        ALERT_SEQ.updateAndGet(current -> Math.max(current, highest));
    }
}
