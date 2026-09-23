package com.bank.model;

import com.bank.enums.UserRole;
import com.bank.interfaces.SecurityOperations;

import java.io.Serial;

/**
 * Represents a bank system administrator with top-level operational authority.
 * Demonstrates Inheritance, Encapsulation, and Polymorphism.
 */
public class Admin extends Person implements SecurityOperations {

    @Serial
    private static final long serialVersionUID = 1L;

    private int securityClearanceLevel;

    public Admin() {
        super();
        this.securityClearanceLevel = 1;
    }

    public Admin(String adminId, String name, String email, String phone, String address, int securityClearanceLevel) {
        super(adminId, name, email, phone, address, UserRole.ADMIN);
        this.securityClearanceLevel = securityClearanceLevel;
    }

    public Admin(String adminId, String name, String email, String phone, String address,
                 int securityClearanceLevel, String pinHash, String pinSalt) {
        super(adminId, name, email, phone, address, UserRole.ADMIN, pinHash, pinSalt);
        this.securityClearanceLevel = securityClearanceLevel;
    }

    public int getSecurityClearanceLevel() {
        return securityClearanceLevel;
    }

    public void setSecurityClearanceLevel(int securityClearanceLevel) {
        this.securityClearanceLevel = securityClearanceLevel;
    }

    @Override
    public void displayDetails() {
        System.out.println("--------------------------------------------------");
        System.out.println(" SYSTEM ADMINISTRATOR DETAILS");
        System.out.println("--------------------------------------------------");
        System.out.println("Admin ID         : " + getId());
        System.out.println("Full Name        : " + getName());
        System.out.println("Clearance Level  : Level " + securityClearanceLevel);
        System.out.println("Email Address    : " + getEmail());
        System.out.println("Phone Number     : " + getPhone());
        System.out.println("Status           : " + (isLocked() ? "LOCKED (" + getLockReason() + ")" : "ACTIVE"));
        System.out.println("--------------------------------------------------");
    }

    @Override
    public String getRoleSpecificInfo() {
        return "System Administrator with Level " + securityClearanceLevel + " clearance";
    }

    @Override
    public void updatePin(String oldPinHash, String newPinHash) {
        setPinCredentials(newPinHash, getPinSalt());
    }
}
