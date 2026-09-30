package com.bank.dto;

import com.bank.enums.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class CustomerRequest {

    @NotBlank(message = "Full legal name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email address cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone must be between 7 and 15 digits")
    private String phone;

    @NotBlank(message = "Residential address cannot be blank")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    private LocalDate dob;
    private String governmentId;

    @NotBlank(message = "Security PIN cannot be blank")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "PIN must be between 4 and 6 numeric digits")
    private String pin;

    private AccountType initialAccountType = AccountType.SAVINGS;
    private double initialDeposit = 1000.0;

    public CustomerRequest() {
    }

    public CustomerRequest(String name, String email, String phone, String address, String pin) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.pin = pin;
        this.dob = LocalDate.of(1995, 1, 1);
        this.governmentId = "ID-" + System.currentTimeMillis() % 100000;
        this.initialAccountType = AccountType.SAVINGS;
        this.initialDeposit = 1000.0;
    }

    public CustomerRequest(String name, String email, String phone, String address,
                           LocalDate dob, String governmentId, String pin,
                           AccountType initialAccountType, double initialDeposit) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dob = dob;
        this.governmentId = governmentId;
        this.pin = pin;
        this.initialAccountType = initialAccountType != null ? initialAccountType : AccountType.SAVINGS;
        this.initialDeposit = initialDeposit > 0 ? initialDeposit : 1000.0;
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

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGovernmentId() {
        return governmentId;
    }

    public void setGovernmentId(String governmentId) {
        this.governmentId = governmentId;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public AccountType getInitialAccountType() {
        return initialAccountType;
    }

    public void setInitialAccountType(AccountType initialAccountType) {
        this.initialAccountType = initialAccountType;
    }

    public double getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(double initialDeposit) {
        this.initialDeposit = initialDeposit;
    }
}
