package com.bank.service;

import com.bank.enums.AlertSeverity;
import com.bank.exceptions.BankingException;
import com.bank.exceptions.CustomerNotFoundException;
import com.bank.exceptions.DuplicateUserException;
import com.bank.exceptions.InvalidPinException;
import com.bank.model.Customer;
import com.bank.repository.CustomerRepository;
import com.bank.security.SecurityService;
import com.bank.util.HashUtil;
import com.bank.util.IdGenerator;
import com.bank.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

/**
 * Service managing Customer registrations, profiles, and credential maintenance.
 */
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SecurityService securityService;

    public CustomerService(CustomerRepository customerRepository, SecurityService securityService) {
        this.customerRepository = customerRepository;
        this.securityService = securityService;
    }

    /**
     * Registers a new customer with input validation, duplicate prevention, and salted PIN hashing.
     */
    public Customer registerCustomer(String name, String email, String phone, String address, String rawPin)
            throws BankingException {
        // Defensive validation
        ValidationUtil.validateNonEmpty(name, "Customer Name");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePhone(phone);
        ValidationUtil.validateNonEmpty(address, "Address");
        ValidationUtil.validatePinFormat(rawPin);

        // Duplicate checks
        if (customerRepository.findByEmail(email.trim()).isPresent()) {
            throw new DuplicateUserException("A customer with email " + email + " is already registered.", email);
        }
        if (customerRepository.findByPhone(phone.trim()).isPresent()) {
            throw new DuplicateUserException("A customer with phone number " + phone + " is already registered.", phone);
        }

        // Generate unique Customer ID and cryptographic salt
        String customerId = IdGenerator.generateCustomerId();
        String salt = HashUtil.generateSalt();
        String pinHash = HashUtil.hashPin(rawPin, salt);

        Customer newCustomer = new Customer(customerId, name.trim(), email.trim(), phone.trim(),
                address.trim(), pinHash, salt);

        customerRepository.save(newCustomer);

        securityService.logSecurityEvent(customerId, null, AlertSeverity.LOW,
                "CUSTOMER_REGISTRATION", "New customer registered with ID: " + customerId);

        return newCustomer;
    }

    public Customer getCustomerById(String customerId) throws CustomerNotFoundException {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer " + customerId + " not found.", customerId));
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> searchCustomersByName(String name) {
        return customerRepository.searchByName(name);
    }

    /**
     * Secure PIN change with old PIN authentication and format validation.
     */
    public void changePin(String customerId, String oldPin, String newPin) throws BankingException {
        Customer customer = getCustomerById(customerId);

        if (customer.isLocked()) {
            throw new BankingException("Cannot change PIN: Account is locked.");
        }

        if (!HashUtil.verifyPin(oldPin, customer.getPinSalt(), customer.getPinHash())) {
            securityService.logSecurityEvent(customerId, null, AlertSeverity.MEDIUM,
                    "FAILED_PIN_CHANGE", "Failed PIN change attempt for " + customerId);
            throw new InvalidPinException("Current PIN is incorrect.");
        }

        ValidationUtil.validatePinFormat(newPin);

        if (oldPin.equals(newPin)) {
            throw new BankingException("New PIN cannot be identical to the current PIN.");
        }

        String newSalt = HashUtil.generateSalt();
        String newHash = HashUtil.hashPin(newPin, newSalt);

        customer.setPinCredentials(newHash, newSalt);
        customerRepository.save(customer);

        securityService.logSecurityEvent(customerId, null, AlertSeverity.LOW,
                "PIN_CHANGE_SUCCESS", "PIN updated successfully for customer " + customerId);
    }
}
