package com.bank.service.impl;

import com.bank.dto.LoginRequest;
import com.bank.dto.LoginResponse;
import com.bank.entity.Customer;
import com.bank.entity.Employee;
import com.bank.exception.AccountLockedException;
import com.bank.exception.BankingException;
import com.bank.repository.CustomerRepository;
import com.bank.repository.EmployeeRepository;
import com.bank.security.JwtUtil;
import com.bank.service.AuthenticationService;
import com.bank.service.FraudDetectionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final FraudDetectionService fraudDetectionService;
    private final JwtUtil jwtUtil;

    public AuthenticationServiceImpl(CustomerRepository customerRepository,
                                     EmployeeRepository employeeRepository,
                                     PasswordEncoder passwordEncoder,
                                     FraudDetectionService fraudDetectionService,
                                     JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.fraudDetectionService = fraudDetectionService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String identifier = request.getUsername().trim();
        String rawPassword = request.getPassword().trim();

        // 1. Check if identifier is an Employee or Admin (starts with EMP- or ADM-)
        Optional<Employee> empOpt = employeeRepository.findByEmployeeId(identifier);
        if (empOpt.isEmpty() && identifier.contains("@")) {
            empOpt = employeeRepository.findByEmail(identifier);
        }

        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            if (emp.isLocked()) {
                throw new AccountLockedException("Staff account is LOCKED. Contact administrator.");
            }
            if (!passwordEncoder.matches(rawPassword, emp.getPasswordHash())) {
                throw new BankingException("Invalid staff credentials entered.");
            }

            String token = jwtUtil.generateToken(
                    emp.getEmployeeId(),
                    emp.getRole().name(),
                    emp.getEmail(),
                    emp.getName()
            );

            return new LoginResponse(
                    emp.getEmployeeId(),
                    emp.getName(),
                    emp.getEmail(),
                    emp.getRole(),
                    "Welcome back, " + emp.getName() + "!",
                    true,
                    token
            );
        }

        // 2. Check Customer login
        Optional<Customer> custOpt = customerRepository.findByCustomerId(identifier);
        if (custOpt.isEmpty() && identifier.contains("@")) {
            custOpt = customerRepository.findByEmail(identifier);
        }

        if (custOpt.isPresent()) {
            Customer cust = custOpt.get();
            if (cust.isLocked()) {
                throw new AccountLockedException("Customer profile is LOCKED: " + cust.getLockReason());
            }

            boolean matches = passwordEncoder.matches(rawPassword, cust.getPinHash());
            if (!matches) {
                cust.setFailedLoginAttempts(cust.getFailedLoginAttempts() + 1);
                fraudDetectionService.evaluateFailedLoginAttempts(cust);

                if (cust.getFailedLoginAttempts() >= FraudDetectionService.MAX_FAILED_PIN_ATTEMPTS) {
                    cust.setLocked(true);
                    cust.setLockReason("Exceeded maximum failed PIN attempts");
                    customerRepository.save(cust);
                    throw new AccountLockedException("Account locked due to 3 consecutive failed PIN attempts.");
                }
                customerRepository.save(cust);
                int remaining = FraudDetectionService.MAX_FAILED_PIN_ATTEMPTS - cust.getFailedLoginAttempts();
                throw new BankingException("Invalid PIN. Remaining attempts: " + remaining);
            }

            // Success
            cust.setFailedLoginAttempts(0);
            customerRepository.save(cust);

            String token = jwtUtil.generateToken(
                    cust.getCustomerId(),
                    cust.getRole().name(),
                    cust.getEmail(),
                    cust.getName()
            );

            return new LoginResponse(
                    cust.getCustomerId(),
                    cust.getName(),
                    cust.getEmail(),
                    cust.getRole(),
                    "Welcome back, " + cust.getName() + "!",
                    true,
                    token
            );
        }

        throw new BankingException("User not found: " + identifier);
    }
}
