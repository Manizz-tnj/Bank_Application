package com.bank.util;

import com.bank.enums.AccountType;
import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {

    private static final AtomicLong CUSTOMER_SEQ = new AtomicLong(System.currentTimeMillis() % 100000 + 10000);
    private static final AtomicLong EMPLOYEE_SEQ = new AtomicLong(System.currentTimeMillis() % 100000 + 20000);
    private static final AtomicLong ADMIN_SEQ = new AtomicLong(System.currentTimeMillis() % 100000 + 30000);
    private static final AtomicLong ACCOUNT_SEQ = new AtomicLong(System.currentTimeMillis() % 100000 + 50000);
    private static final AtomicLong TXN_SEQ = new AtomicLong(System.currentTimeMillis() % 100000 + 70000);
    private static final AtomicLong ALERT_SEQ = new AtomicLong(System.currentTimeMillis() % 100000 + 90000);
    private static final AtomicLong LEDGER_SEQ = new AtomicLong(System.currentTimeMillis() % 100000 + 40000);

    private IdGenerator() {
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

    public static String generateLedgerId() {
        return "LDG-" + LEDGER_SEQ.incrementAndGet();
    }
}
