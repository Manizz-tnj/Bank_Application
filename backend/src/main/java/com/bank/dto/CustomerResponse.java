package com.bank.dto;

import com.bank.enums.UserRole;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CustomerResponse {

    private Long id;
    private String customerId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDate dob;
    private String governmentId;
    private String kycStatus;
    private String branchName;
    private String routingNumber;
    private UserRole role;
    private boolean locked;
    private String lockReason;
    private LocalDateTime createdAt;
    private List<String> accountIds;

    public CustomerResponse() {
    }

    public CustomerResponse(Long id, String customerId, String name, String email, String phone,
                            String address, LocalDate dob, String governmentId, String kycStatus,
                            String branchName, String routingNumber, UserRole role, boolean locked,
                            String lockReason, LocalDateTime createdAt, List<String> accountIds) {
        this.id = id;
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dob = dob;
        this.governmentId = governmentId;
        this.kycStatus = kycStatus;
        this.branchName = branchName;
        this.routingNumber = routingNumber;
        this.role = role;
        this.locked = locked;
        this.lockReason = lockReason;
        this.createdAt = createdAt;
        this.accountIds = accountIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getLockReason() {
        return lockReason;
    }

    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds;
    }
}
