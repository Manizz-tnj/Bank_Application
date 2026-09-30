package com.bank.service;

import com.bank.dto.LoginRequest;
import com.bank.dto.LoginResponse;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);
}
