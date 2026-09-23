package com.bank.service;

import com.bank.enums.AlertSeverity;
import com.bank.enums.UserRole;
import com.bank.exceptions.AccountLockedException;
import com.bank.exceptions.BankingException;
import com.bank.exceptions.CustomerNotFoundException;
import com.bank.exceptions.InvalidPinException;
import com.bank.model.Bank;
import com.bank.model.Customer;
import com.bank.model.Person;
import com.bank.repository.CustomerRepository;
import com.bank.repository.EmployeeRepository;
import com.bank.security.FraudContext;
import com.bank.security.FraudDetectionService;
import com.bank.security.SecurityService;
import com.bank.util.HashUtil;

import java.util.Optional;

/**
 * Service orchestrating user authentication, credential validation,
 * failed attempt rate-limiting, and automatic account lockouts.
 */
public class AuthenticationService {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityService securityService;
    private final FraudDetectionService fraudDetectionService;

    public AuthenticationService(CustomerRepository customerRepository,
                                 EmployeeRepository employeeRepository,
                                 SecurityService securityService,
                                 FraudDetectionService fraudDetectionService) {
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.securityService = securityService;
        this.fraudDetectionService = fraudDetectionService;
    }

    /**
     * Authenticates a Customer using their customerId and numeric PIN.
     */
    public Customer authenticateCustomer(String customerId, String pin) throws BankingException {
        if (customerId == null || customerId.trim().isEmpty() || pin == null) {
            throw new BankingException("Customer ID and PIN cannot be blank.");
        }

        Customer customer = customerRepository.findById(customerId.trim())
                .orElseThrow(() -> new CustomerNotFoundException("Customer account not found: " + customerId, customerId));

        if (customer.isLocked()) {
            throw new AccountLockedException("Customer account is LOCKED: " + customer.getLockReason(), customerId);
        }

        boolean pinValid = HashUtil.verifyPin(pin, customer.getPinSalt(), customer.getPinHash());
        if (!pinValid) {
            handleFailedCustomerLogin(customer);
        }

        // Authentication Successful
        customer.resetFailedAttempts();
        customerRepository.save(customer);

        securityService.logSecurityEvent(customer.getId(), null, AlertSeverity.LOW,
                "CUSTOMER_LOGIN_SUCCESS", "Customer " + customer.getId() + " logged in successfully.");

        return customer;
    }

    private void handleFailedCustomerLogin(Customer customer) throws BankingException {
        customer.incrementFailedAttempts();
        int attempts = customer.getFailedLoginAttempts();
        int remaining = Bank.MAX_FAILED_LOGIN_ATTEMPTS - attempts;

        // Run fraud detection on failed login attempt
        FraudContext context = new FraudContext(customer, null, null, attempts, null, "Customer login attempt");
        fraudDetectionService.evaluate(context);

        if (attempts >= Bank.MAX_FAILED_LOGIN_ATTEMPTS) {
            customer.lock("Exceeded maximum failed PIN attempts (" + attempts + ")");
            customerRepository.save(customer);
            throw new AccountLockedException("Maximum login attempts exceeded. Account " + customer.getId() + " is now LOCKED.", customer.getId());
        }

        customerRepository.save(customer);
        throw new InvalidPinException("Invalid PIN entered. Attempts remaining before lockout: " + remaining, remaining);
    }

    /**
     * Authenticates an Employee or Admin staff member.
     */
    public Person authenticateStaff(String staffId, String pin, UserRole expectedRole) throws BankingException {
        if (staffId == null || staffId.trim().isEmpty() || pin == null) {
            throw new BankingException("Staff ID and PIN cannot be blank.");
        }

        Person staff = employeeRepository.findById(staffId.trim())
                .orElseThrow(() -> new BankingException("Staff account not found: " + staffId));

        if (staff.getRole() != expectedRole) {
            throw new BankingException("Invalid role permissions for " + staffId + ". Expected: " + expectedRole);
        }

        if (staff.isLocked()) {
            throw new AccountLockedException("Staff profile is LOCKED: " + staff.getLockReason(), staffId);
        }

        boolean pinValid = HashUtil.verifyPin(pin, staff.getPinSalt(), staff.getPinHash());
        if (!pinValid) {
            staff.incrementFailedAttempts();
            int remaining = Bank.MAX_FAILED_LOGIN_ATTEMPTS - staff.getFailedLoginAttempts();
            if (staff.getFailedLoginAttempts() >= Bank.MAX_FAILED_LOGIN_ATTEMPTS) {
                staff.lock("Exceeded maximum failed PIN attempts");
                employeeRepository.save(staff);
                throw new AccountLockedException("Staff account locked due to excessive failed attempts.", staffId);
            }
            employeeRepository.save(staff);
            throw new InvalidPinException("Invalid staff PIN. Attempts remaining: " + remaining, remaining);
        }

        staff.resetFailedAttempts();
        employeeRepository.save(staff);

        securityService.logSecurityEvent(staff.getId(), null, AlertSeverity.LOW,
                "STAFF_LOGIN_SUCCESS", staff.getRole() + " " + staff.getId() + " logged in.");

        return staff;
    }
}
