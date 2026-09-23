package com.bank.model;

import com.bank.enums.UserRole;
import com.bank.interfaces.SecurityOperations;

import java.io.Serial;

/**
 * Represents a bank employee (teller, clerk, branch officer).
 * Demonstrates Inheritance, Encapsulation, and Polymorphism.
 */
public class Employee extends Person implements SecurityOperations {

    @Serial
    private static final long serialVersionUID = 1L;

    private String department;
    private String designation;

    public Employee() {
        super();
        this.department = "General Operations";
        this.designation = "Bank Officer";
    }

    public Employee(String employeeId, String name, String email, String phone, String address,
                    String department, String designation) {
        super(employeeId, name, email, phone, address, UserRole.EMPLOYEE);
        this.department = department;
        this.designation = designation;
    }

    public Employee(String employeeId, String name, String email, String phone, String address,
                    String department, String designation, String pinHash, String pinSalt) {
        super(employeeId, name, email, phone, address, UserRole.EMPLOYEE, pinHash, pinSalt);
        this.department = department;
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    @Override
    public void displayDetails() {
        System.out.println("--------------------------------------------------");
        System.out.println(" EMPLOYEE PROFILE DETAILS");
        System.out.println("--------------------------------------------------");
        System.out.println("Employee ID     : " + getId());
        System.out.println("Full Name       : " + getName());
        System.out.println("Department      : " + department);
        System.out.println("Designation     : " + designation);
        System.out.println("Email Address   : " + getEmail());
        System.out.println("Phone Number    : " + getPhone());
        System.out.println("Status          : " + (isLocked() ? "LOCKED (" + getLockReason() + ")" : "ACTIVE"));
        System.out.println("--------------------------------------------------");
    }

    @Override
    public String getRoleSpecificInfo() {
        return designation + " in " + department;
    }

    @Override
    public void updatePin(String oldPinHash, String newPinHash) {
        setPinCredentials(newPinHash, getPinSalt());
    }
}
