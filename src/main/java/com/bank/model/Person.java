package com.bank.model;

import com.bank.enums.UserRole;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class representing any individual in the banking ecosystem.
 * Demonstrates Abstraction, Encapsulation, and Inheritance.
 */
public abstract class Person implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Encapsulated private fields
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private UserRole role;
    private String pinHash;
    private String pinSalt;
    private int failedLoginAttempts;
    private boolean locked;
    private String lockReason;
    private LocalDateTime registrationDate;

    /**
     * Default constructor demonstrating constructor chaining.
     */
    protected Person() {
        this("", "", "", "", "", UserRole.CUSTOMER);
    }

    /**
     * Parameterized constructor without credentials.
     */
    protected Person(String id, String name, String email, String phone, String address, UserRole role) {
        this(id, name, email, phone, address, role, null, null);
    }

    /**
     * Full parameterized constructor demonstrating constructor chaining and initialization.
     */
    protected Person(String id, String name, String email, String phone, String address,
                     UserRole role, String pinHash, String pinSalt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.pinHash = pinHash;
        this.pinSalt = pinSalt;
        this.failedLoginAttempts = 0;
        this.locked = false;
        this.lockReason = null;
        this.registrationDate = LocalDateTime.now();
    }

    // Abstract method forcing concrete subclasses to present role-specific details
    public abstract void displayDetails();

    public abstract String getRoleSpecificInfo();

    // Controlled modifications for security-related state
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void lock(String reason) {
        this.locked = true;
        this.lockReason = reason;
    }

    public void unlock() {
        this.locked = false;
        this.lockReason = null;
        this.failedLoginAttempts = 0;
    }

    // Getters and Setters with Encapsulation
    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UserRole getRole() {
        return role;
    }

    protected void setRole(UserRole role) {
        this.role = role;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinCredentials(String pinHash, String pinSalt) {
        this.pinHash = pinHash;
        this.pinSalt = pinSalt;
    }

    public String getPinSalt() {
        return pinSalt;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getLockReason() {
        return lockReason;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) return false;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | Name: %s | Email: %s | Status: %s",
                role != null ? role.getDisplayName() : "Unknown",
                id, name, email, (locked ? "LOCKED (" + lockReason + ")" : "ACTIVE"));
    }
}
