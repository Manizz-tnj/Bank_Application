package com.bank.service.impl;

import com.bank.dto.AccountRequest;
import com.bank.dto.AccountResponse;
import com.bank.dto.DepositWithdrawRequest;
import com.bank.entity.Account;
import com.bank.entity.Customer;
import com.bank.entity.LedgerEntry;
import com.bank.entity.Transaction;
import com.bank.enums.AccountStatus;
import com.bank.enums.AccountType;
import com.bank.enums.LedgerEntryType;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.BankingException;
import com.bank.exception.CustomerNotFoundException;
import com.bank.exception.InvalidAmountException;
import com.bank.mapper.EntityDtoMapper;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.LedgerEntryRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.AccountService;
import com.bank.service.FraudDetectionService;
import com.bank.util.IdGenerator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final FraudDetectionService fraudDetectionService;

    public AccountServiceImpl(AccountRepository accountRepository,
                              CustomerRepository customerRepository,
                              TransactionRepository transactionRepository,
                              LedgerEntryRepository ledgerEntryRepository,
                              FraudDetectionService fraudDetectionService) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.fraudDetectionService = fraudDetectionService;
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                ? auth.getName() : "SYSTEM";
    }

    @Override
    public AccountResponse createAccount(AccountRequest request) {
        Customer customer = customerRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer " + request.getCustomerId() + " not found"));

        if (customer.isLocked()) {
            throw new BankingException("Cannot open account: Customer profile is locked");
        }

        if (request.getAccountType() == AccountType.SAVINGS && request.getInitialDeposit() < 1000.0) {
            throw new InvalidAmountException("Savings account requires an initial minimum deposit of $1,000.00", request.getInitialDeposit());
        }
        if (request.getAccountType() == AccountType.CURRENT && request.getInitialDeposit() < 2000.0) {
            throw new InvalidAmountException("Current account requires an initial minimum deposit of $2,000.00", request.getInitialDeposit());
        }
        if (request.getAccountType() == AccountType.FIXED_DEPOSIT && request.getInitialDeposit() < 5000.0) {
            throw new InvalidAmountException("Fixed Deposit requires an initial minimum deposit of $5,000.00", request.getInitialDeposit());
        }

        String accountId = IdGenerator.generateAccountId(request.getAccountType());
        Account account = new Account(accountId, customer, request.getAccountType(), request.getInitialDeposit());

        if (request.getAccountType() == AccountType.CURRENT && request.getOverdraftLimit() > 0) {
            account.setOverdraftLimit(request.getOverdraftLimit());
        }
        if (request.getAccountType() == AccountType.FIXED_DEPOSIT && request.getTermMonths() != null) {
            account.setTermMonths(request.getTermMonths());
            account.setMaturityDate(account.getCreatedAt().plusMonths(request.getTermMonths()));
        }

        Account saved = accountRepository.save(account);

        // Record opening transaction
        Transaction openTxn = new Transaction(
                IdGenerator.generateTransactionId(),
                saved,
                TransactionType.DEPOSIT,
                request.getInitialDeposit(),
                "Initial deposit - " + request.getAccountType() + " opening",
                TransactionStatus.SUCCESS,
                null
        );
        Transaction savedTxn = transactionRepository.save(openTxn);

        // Record opening Ledger entry
        LedgerEntry openLedger = new LedgerEntry(
                IdGenerator.generateLedgerId(),
                savedTxn.getTransactionId(),
                saved.getAccountId(),
                LedgerEntryType.CREDIT,
                request.getInitialDeposit(),
                saved.getBalance(),
                savedTxn.getDescription(),
                getCurrentUser()
        );
        ledgerEntryRepository.save(openLedger);

        return EntityDtoMapper.toAccountResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(String accountId) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found"));
        return EntityDtoMapper.toAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsForCustomer(String customerId) {
        return accountRepository.findByCustomerCustomerId(customerId).stream()
                .map(EntityDtoMapper::toAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(EntityDtoMapper::toAccountResponse)
                .toList();
    }

    @Override
    public AccountResponse deposit(String accountId, DepositWithdrawRequest request) {
        // Enforce pessimistic write locking
        Account account = accountRepository.findByAccountIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found"));

        account.deposit(request.getAmount());
        Account saved = accountRepository.save(account);

        String note = (request.getDescription() != null && !request.getDescription().isBlank())
                ? request.getDescription() : "Cash/Cheque Deposit";

        Transaction txn = new Transaction(
                IdGenerator.generateTransactionId(),
                saved,
                TransactionType.DEPOSIT,
                request.getAmount(),
                note,
                TransactionStatus.SUCCESS,
                null
        );
        Transaction savedTxn = transactionRepository.save(txn);

        // Double-entry ledger movement (CREDIT)
        LedgerEntry ledger = new LedgerEntry(
                IdGenerator.generateLedgerId(),
                savedTxn.getTransactionId(),
                saved.getAccountId(),
                LedgerEntryType.CREDIT,
                request.getAmount(),
                saved.getBalance(),
                note,
                getCurrentUser()
        );
        ledgerEntryRepository.save(ledger);

        // Run fraud rules
        fraudDetectionService.evaluateLargeTransaction(saved, txn);

        return EntityDtoMapper.toAccountResponse(saved);
    }

    @Override
    public AccountResponse withdraw(String accountId, DepositWithdrawRequest request) {
        // Enforce pessimistic write locking
        Account account = accountRepository.findByAccountIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found"));

        account.withdraw(request.getAmount());
        Account saved = accountRepository.save(account);

        String note = (request.getDescription() != null && !request.getDescription().isBlank())
                ? request.getDescription() : "Cash Withdrawal";

        Transaction txn = new Transaction(
                IdGenerator.generateTransactionId(),
                saved,
                TransactionType.WITHDRAWAL,
                request.getAmount(),
                note,
                TransactionStatus.SUCCESS,
                null
        );
        Transaction savedTxn = transactionRepository.save(txn);

        // Double-entry ledger movement (DEBIT)
        LedgerEntry ledger = new LedgerEntry(
                IdGenerator.generateLedgerId(),
                savedTxn.getTransactionId(),
                saved.getAccountId(),
                LedgerEntryType.DEBIT,
                request.getAmount(),
                saved.getBalance(),
                note,
                getCurrentUser()
        );
        ledgerEntryRepository.save(ledger);

        // Run fraud rules
        fraudDetectionService.evaluateLargeTransaction(saved, txn);
        fraudDetectionService.evaluateUnusualActivity(saved, txn);

        return EntityDtoMapper.toAccountResponse(saved);
    }

    @Override
    public void closeAccount(String accountId) {
        Account account = accountRepository.findByAccountIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found"));

        if (account.getBalance() > 0.01) {
            throw new BankingException("Cannot close account with remaining balance of $" +
                    String.format("%.2f", account.getBalance()) + ". Please withdraw all funds first.");
        }
        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    @Override
    public void lockAccount(String accountId, String reason) {
        Account account = accountRepository.findByAccountIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found"));
        account.setStatus(AccountStatus.LOCKED);
        account.setLockReason(reason);
        accountRepository.save(account);
    }

    @Override
    public void unlockAccount(String accountId) {
        Account account = accountRepository.findByAccountIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setLockReason(null);
        accountRepository.save(account);
    }
}
