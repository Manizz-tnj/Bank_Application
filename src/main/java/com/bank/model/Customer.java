package com.bank.model;

import com.bank.enums.UserRole;
import com.bank.interfaces.SecurityOperations;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a bank retail or corporate customer.
 * Demonstrates Inheritance, Encapsulation, Association (with Accounts),
 * and Interface implementation.
 */
public class Customer extends Person implements SecurityOperations {

    @Serial
    private static final long serialVersionUID = 1L;

    // Association / Aggregation: Holds reference IDs to associated accounts
    private final List<String> accountIds;

    /**
     * Default constructor.
     */
    public Customer() {
        super();
        this.accountIds = new ArrayList<>();
    }

    /**
     * Parameterized constructor using constructor chaining.
     */
    public Customer(String customerId, String name, String email, String phone, String address) {
        super(customerId, name, email, phone, address, UserRole.CUSTOMER);
        this.accountIds = new ArrayList<>();
    }

    /**
     * Full parameterized constructor with credentials.
     */
    public Customer(String customerId, String name, String email, String phone, String address,
                    String pinHash, String pinSalt) {
        super(customerId, name, email, phone, address, UserRole.CUSTOMER, pinHash, pinSalt);
        this.accountIds = new ArrayList<>();
    }

    /**
     * Associates an account with this customer.
     */
    public void addAccountId(String accountId) {
        if (accountId != null && !accountIds.contains(accountId)) {
            accountIds.add(accountId);
        }
    }

    /**
     * Disassociates an account from this customer.
     */
    public boolean removeAccountId(String accountId) {
        return accountIds.remove(accountId);
    }

    /**
     * Returns an unmodifiable view of account IDs for defensive copying.
     */
    public List<String> getAccountIds() {
        return Collections.unmodifiableList(accountIds);
    }

    public boolean hasAccount(String accountId) {
        return accountIds.contains(accountId);
    }

    public int getAccountCount() {
        return accountIds.size();
    }

    @Override
    public void displayDetails() {
        System.out.println("--------------------------------------------------");
        System.out.println(" CUSTOMER PROFILE DETAILS");
        System.out.println("--------------------------------------------------");
        System.out.println("Customer ID      : " + getId());
        System.out.println("Full Name        : " + getName());
        System.out.println("Email Address    : " + getEmail());
        System.out.println("Phone Number     : " + getPhone());
        System.out.println("Residential Addr : " + getAddress());
        System.out.println("Registered On    : " + getRegistrationDate());
        System.out.println("Account Status   : " + (isLocked() ? "LOCKED (" + getLockReason() + ")" : "ACTIVE"));
        System.out.println("Linked Accounts  : " + (accountIds.isEmpty() ? "None" : String.join(", ", accountIds)));
        System.out.println("--------------------------------------------------");
    }

    @Override
    public String getRoleSpecificInfo() {
        return "Customer with " + accountIds.size() + " registered account(s).";
    }

    // Implementing SecurityOperations interface
    @Override
    public void updatePin(String oldPinHash, String newPinHash) {
        // Validation handled in Authentication/Customer Service; updates credential
        setPinCredentials(newPinHash, getPinSalt());
    }
}
