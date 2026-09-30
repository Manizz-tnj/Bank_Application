package com.bank.dto;

import com.bank.enums.UserRole;

public class LoginResponse {

    private String userId;
    private String name;
    private String email;
    private UserRole role;
    private String message;
    private boolean success;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(String userId, String name, String email, UserRole role, String message, boolean success) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.message = message;
        this.success = success;
    }

    public LoginResponse(String userId, String name, String email, UserRole role, String message, boolean success, String token) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.message = message;
        this.success = success;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
