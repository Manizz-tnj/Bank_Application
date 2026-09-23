package com.bank.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents the Banking Institution.
 * Demonstrates Aggregation and static configuration constants.
 */
public class Bank implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_BANK_NAME = "APEX NATIONAL BANK";
    public static final String DEFAULT_BRANCH = "CENTRAL METROPOLITAN";
    public static final String DEFAULT_SWIFT = "APEXUS33";

    // Configurable fraud & security thresholds
    public static final double LARGE_TRANSACTION_THRESHOLD = 50000.0;
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;
    public static final int RAPID_TRANSFER_VELOCITY_LIMIT = 3;
    public static final int RAPID_TRANSFER_WINDOW_MINUTES = 5;

    private final String bankName;
    private final String branch;
    private final String swiftCode;

    public Bank() {
        this(DEFAULT_BANK_NAME, DEFAULT_BRANCH, DEFAULT_SWIFT);
    }

    public Bank(String bankName, String branch, String swiftCode) {
        this.bankName = bankName;
        this.branch = branch;
        this.swiftCode = swiftCode;
    }

    public String getBankName() {
        return bankName;
    }

    public String getBranch() {
        return branch;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public void displayBankInfo() {
        System.out.println("==================================================");
        System.out.println(" INSTITUTION INFORMATION");
        System.out.println("==================================================");
        System.out.println("Bank Name       : " + bankName);
        System.out.println("Branch Name     : " + branch);
        System.out.println("SWIFT / Code    : " + swiftCode);
        System.out.println("Large Txn Limit : $" + String.format("%,.2f", LARGE_TRANSACTION_THRESHOLD));
        System.out.println("Max Failed PINs : " + MAX_FAILED_LOGIN_ATTEMPTS);
        System.out.println("==================================================");
    }
}
