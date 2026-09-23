package com.bank.service;

import com.bank.enums.AccountStatus;
import com.bank.enums.AccountType;
import com.bank.enums.AlertSeverity;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import com.bank.exceptions.AccountNotFoundException;
import com.bank.exceptions.BankingException;
import com.bank.exceptions.CustomerNotFoundException;
import com.bank.exceptions.UnauthorizedOperationException;
import com.bank.model.*;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;
import com.bank.security.SecurityService;
import com.bank.util.IdGenerator;
import com.bank.util.ValidationUtil;

import java.util.List;

/**
 * Service managing Account creation (Savings, Current, Fixed Deposit),
 * ownership authorization, balance verification, and account closure.
 */
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final SecurityService securityService;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          TransactionRepository transactionRepository,
                          SecurityService securityService) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.securityService = securityService;
    }

    /**
     * Creates a Savings Account for an existing customer.
     */
    public SavingsAccount createSavingsAccount(String customerId, double initialDeposit) throws BankingException {
        Customer customer = findAndValidateCustomer(customerId);
        ValidationUtil.validatePositiveAmount(initialDeposit, "Initial deposit");
        if (initialDeposit < SavingsAccount.DEFAULT_MINIMUM_BALANCE) {
            throw new BankingException(String.format("Initial deposit ($%,.2f) must meet minimum balance requirement ($%,.2f).",
                    initialDeposit, SavingsAccount.DEFAULT_MINIMUM_BALANCE));
        }

        String accountId = IdGenerator.generateAccountId(AccountType.SAVINGS);
        SavingsAccount account = new SavingsAccount(accountId, customerId, initialDeposit);

        accountRepository.save(account);
        customer.addAccountId(accountId);
        customerRepository.save(customer);

        recordAccountOpeningTransaction(accountId, initialDeposit, "Initial deposit - Savings account opening");

        securityService.logSecurityEvent(customerId, accountId, AlertSeverity.LOW,
                "ACCOUNT_OPENED", "Savings Account " + accountId + " opened for " + customerId);

        return account;
    }

    /**
     * Creates a Current Account with overdraft facility.
     */
    public CurrentAccount createCurrentAccount(String customerId, double initialDeposit, double overdraftLimit)
            throws BankingException {
        Customer customer = findAndValidateCustomer(customerId);
        ValidationUtil.validatePositiveAmount(initialDeposit, "Initial deposit");
        double limit = overdraftLimit > 0 ? overdraftLimit : CurrentAccount.DEFAULT_OVERDRAFT_LIMIT;

        String accountId = IdGenerator.generateAccountId(AccountType.CURRENT);
        CurrentAccount account = new CurrentAccount(accountId, customerId, initialDeposit, limit);

        accountRepository.save(account);
        customer.addAccountId(accountId);
        customerRepository.save(customer);

        recordAccountOpeningTransaction(accountId, initialDeposit, "Initial deposit - Current account opening");

        securityService.logSecurityEvent(customerId, accountId, AlertSeverity.LOW,
                "ACCOUNT_OPENED", "Current Account " + accountId + " opened for " + customerId);

        return account;
    }

    /**
     * Creates a Fixed Deposit Account for a specified term in months.
     */
    public FixedDepositAccount createFixedDepositAccount(String customerId, double depositAmount, int termMonths)
            throws BankingException {
        Customer customer = findAndValidateCustomer(customerId);
        ValidationUtil.validatePositiveAmount(depositAmount, "Fixed Deposit amount");
        if (termMonths < 3) {
            throw new BankingException("Minimum Fixed Deposit maturity term is 3 months.");
        }

        String accountId = IdGenerator.generateAccountId(AccountType.FIXED_DEPOSIT);
        FixedDepositAccount account = new FixedDepositAccount(accountId, customerId, depositAmount, termMonths);

        accountRepository.save(account);
        customer.addAccountId(accountId);
        customerRepository.save(customer);

        recordAccountOpeningTransaction(accountId, depositAmount, "Principal deposit - Fixed Deposit term (" + termMonths + "m)");

        securityService.logSecurityEvent(customerId, accountId, AlertSeverity.LOW,
                "ACCOUNT_OPENED", "Fixed Deposit Account " + accountId + " opened for " + customerId);

        return account;
    }

    public Account getAccountById(String accountId) throws AccountNotFoundException {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found.", accountId));
    }

    public List<Account> getAccountsForCustomer(String customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    /**
     * Critical Security Check: Validates that an account belongs to the logged-in customer.
     * Prevents Customer A from accessing Customer B's account.
     */
    public Account validateOwnership(String customerId, String accountId) throws BankingException {
        Account account = getAccountById(accountId);
        if (!account.getCustomerId().equals(customerId)) {
            securityService.logSecurityEvent(customerId, accountId, AlertSeverity.HIGH,
                    "UNAUTHORIZED_ACCOUNT_ACCESS",
                    String.format("Customer %s attempted unauthorized access to account %s owned by %s.",
                            customerId, accountId, account.getCustomerId()));
            throw new UnauthorizedOperationException(
                    "Access Denied: You do not own account " + accountId, customerId, "ACCESS_ACCOUNT");
        }
        return account;
    }

    /**
     * Closes an account if balance is zero and user is verified owner.
     */
    public void closeAccount(String customerId, String accountId) throws BankingException {
        Account account = validateOwnership(customerId, accountId);
        account.closeAccount();
        accountRepository.save(account);

        securityService.logSecurityEvent(customerId, accountId, AlertSeverity.MEDIUM,
                "ACCOUNT_CLOSED", "Account " + accountId + " closed by customer " + customerId);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    private Customer findAndValidateCustomer(String customerId) throws BankingException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer " + customerId + " not found.", customerId));
        if (customer.isLocked()) {
            throw new BankingException("Cannot open account: Customer profile " + customerId + " is locked.");
        }
        return customer;
    }

    private void recordAccountOpeningTransaction(String accountId, double amount, String description) {
        Transaction txn = new Transaction(
                IdGenerator.generateTransactionId(),
                accountId,
                TransactionType.DEPOSIT,
                amount,
                description,
                TransactionStatus.SUCCESS,
                null
        );
        transactionRepository.save(txn);
    }
}
