package com.bank.service.impl;

import com.bank.dto.CustomerRequest;
import com.bank.dto.CustomerResponse;
import com.bank.entity.Account;
import com.bank.entity.Customer;
import com.bank.entity.LedgerEntry;
import com.bank.entity.Transaction;
import com.bank.enums.AccountType;
import com.bank.enums.LedgerEntryType;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import com.bank.exception.CustomerNotFoundException;
import com.bank.exception.DuplicateUserException;
import com.bank.exception.InvalidAmountException;
import com.bank.mapper.EntityDtoMapper;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.LedgerEntryRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.CustomerService;
import com.bank.util.IdGenerator;
import com.bank.util.PasswordValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               AccountRepository accountRepository,
                               TransactionRepository transactionRepository,
                               LedgerEntryRepository ledgerEntryRepository,
                               PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public CustomerResponse registerCustomer(CustomerRequest request) {
        PasswordValidator.validatePin(request.getPin().trim());

        if (customerRepository.findByEmail(request.getEmail().trim()).isPresent()) {
            throw new DuplicateUserException("A customer with email " + request.getEmail() + " is already registered.");
        }
        if (customerRepository.findByPhone(request.getPhone().trim()).isPresent()) {
            throw new DuplicateUserException("A customer with phone " + request.getPhone() + " is already registered.");
        }

        String customerId = IdGenerator.generateCustomerId();
        String encodedPin = passwordEncoder.encode(request.getPin().trim());

        Customer customer = new Customer(
                customerId,
                request.getName().trim(),
                request.getEmail().trim(),
                request.getPhone().trim(),
                request.getAddress().trim(),
                request.getDob(),
                request.getGovernmentId(),
                encodedPin
        );

        Customer savedCustomer = customerRepository.save(customer);

        // Real Bank Auto-Onboarding: Immediately open customer's primary account
        AccountType accType = request.getInitialAccountType() != null ? request.getInitialAccountType() : AccountType.SAVINGS;
        double minReq = accType == AccountType.CURRENT ? 2000.0 : (accType == AccountType.FIXED_DEPOSIT ? 5000.0 : 1000.0);
        double deposit = request.getInitialDeposit() >= minReq ? request.getInitialDeposit() : minReq;

        String accountId = IdGenerator.generateAccountId(accType);
        Account primaryAccount = new Account(accountId, savedCustomer, accType, deposit);
        Account savedAccount = accountRepository.save(primaryAccount);
        savedCustomer.addAccount(savedAccount);

        Transaction openingTxn = new Transaction(
                IdGenerator.generateTransactionId(),
                savedAccount,
                TransactionType.DEPOSIT,
                deposit,
                "Initial Account Opening Deposit - " + accType.getDescription(),
                TransactionStatus.SUCCESS,
                null
        );
        Transaction savedTxn = transactionRepository.save(openingTxn);

        // Double-Entry Ledger: opening balance credit
        LedgerEntry openingLedger = new LedgerEntry(
                IdGenerator.generateLedgerId(),
                savedTxn.getTransactionId(),
                savedAccount.getAccountId(),
                LedgerEntryType.CREDIT,
                deposit,
                deposit,
                savedTxn.getDescription(),
                "SYSTEM"
        );
        ledgerEntryRepository.save(openingLedger);

        return EntityDtoMapper.toCustomerResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(String customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer " + customerId + " not found"));
        return EntityDtoMapper.toCustomerResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(EntityDtoMapper::toCustomerResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> searchCustomers(String name) {
        if (name == null || name.isBlank()) {
            return getAllCustomers();
        }
        return customerRepository.findByNameContainingIgnoreCase(name.trim()).stream()
                .map(EntityDtoMapper::toCustomerResponse)
                .toList();
    }

    @Override
    public CustomerResponse updateCustomer(String customerId, CustomerRequest request) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer " + customerId + " not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            customer.setName(request.getName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            customer.setEmail(request.getEmail().trim());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            customer.setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            customer.setAddress(request.getAddress().trim());
        }

        Customer updated = customerRepository.save(customer);
        return EntityDtoMapper.toCustomerResponse(updated);
    }

    @Override
    public void changePin(String customerId, String oldPin, String newPin) {
        PasswordValidator.validatePin(newPin.trim());

        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer " + customerId + " not found"));

        if (!passwordEncoder.matches(oldPin, customer.getPinHash())) {
            throw new InvalidAmountException("Current PIN is incorrect", 0);
        }

        customer.setPinHash(passwordEncoder.encode(newPin.trim()));
        customerRepository.save(customer);
    }

    @Override
    public void lockCustomer(String customerId, String reason) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer " + customerId + " not found"));
        customer.setLocked(true);
        customer.setLockReason(reason);
        customerRepository.save(customer);
    }

    @Override
    public void unlockCustomer(String customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer " + customerId + " not found"));
        customer.setLocked(false);
        customer.setLockReason(null);
        customer.setFailedLoginAttempts(0);
        customerRepository.save(customer);
    }
}
