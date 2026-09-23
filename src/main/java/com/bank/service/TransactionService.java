package com.bank.service;

import com.bank.enums.AlertSeverity;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import com.bank.exceptions.AccountNotFoundException;
import com.bank.exceptions.BankingException;
import com.bank.exceptions.InvalidTransferException;
import com.bank.model.Account;
import com.bank.model.Bank;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;
import com.bank.security.FraudContext;
import com.bank.security.FraudDetectionService;
import com.bank.security.SecurityService;
import com.bank.util.IdGenerator;
import com.bank.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

/**
 * Service orchestrating financial deposits, withdrawals, fund transfers,
 * and security fraud checks.
 */
public class TransactionService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final SecurityService securityService;
    private final AccountService accountService;

    public TransactionService(AccountRepository accountRepository,
                              CustomerRepository customerRepository,
                              TransactionRepository transactionRepository,
                              FraudDetectionService fraudDetectionService,
                              SecurityService securityService,
                              AccountService accountService) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.securityService = securityService;
        this.accountService = accountService;
    }

    /**
     * Executes a deposit into an account.
     */
    public Transaction deposit(String customerId, String accountId, double amount, String description)
            throws BankingException {
        Account account = accountService.validateOwnership(customerId, accountId);
        ValidationUtil.validatePositiveAmount(amount, "Deposit");

        String note = (description != null && !description.isBlank()) ? description : "Cash/Cheque Deposit";
        account.deposit(amount, note);
        accountRepository.save(account);

        Transaction txn = new Transaction(
                IdGenerator.generateTransactionId(),
                accountId,
                TransactionType.DEPOSIT,
                amount,
                note,
                TransactionStatus.SUCCESS,
                null
        );
        transactionRepository.save(txn);

        // Evaluate fraud rules (e.g. large transaction rule)
        Customer customer = customerRepository.findById(customerId).orElse(null);
        FraudContext fraudContext = new FraudContext(customer, account, txn, 0, null, "Deposit evaluation");
        fraudDetectionService.evaluate(fraudContext);

        return txn;
    }

    /**
     * Executes a withdrawal from an account.
     */
    public Transaction withdraw(String customerId, String accountId, double amount, String description)
            throws BankingException {
        Account account = accountService.validateOwnership(customerId, accountId);
        ValidationUtil.validatePositiveAmount(amount, "Withdrawal");

        String note = (description != null && !description.isBlank()) ? description : "Cash Withdrawal";
        account.withdraw(amount, note);
        accountRepository.save(account);

        Transaction txn = new Transaction(
                IdGenerator.generateTransactionId(),
                accountId,
                TransactionType.WITHDRAWAL,
                amount,
                note,
                TransactionStatus.SUCCESS,
                null
        );
        transactionRepository.save(txn);

        Customer customer = customerRepository.findById(customerId).orElse(null);
        FraudContext fraudContext = new FraudContext(customer, account, txn, 0, null, "Withdrawal evaluation");
        fraudDetectionService.evaluate(fraudContext);

        return txn;
    }

    /**
     * Transfers money from source account to destination account.
     * Validates ownership, checks both accounts, and creates audit records for both parties.
     */
    public Transaction transfer(String customerId, String fromAccountId, String toAccountId,
                                double amount, String description) throws BankingException {
        // Validate sender ownership
        Account sourceAccount = accountService.validateOwnership(customerId, fromAccountId);
        ValidationUtil.validatePositiveAmount(amount, "Transfer");

        if (fromAccountId.equalsIgnoreCase(toAccountId)) {
            throw new InvalidTransferException("Cannot transfer money to the same account: " + fromAccountId);
        }

        // Validate destination account
        Account destAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Destination account " + toAccountId + " not found.", toAccountId));

        if (!destAccount.isActive()) {
            throw new InvalidTransferException("Destination account " + toAccountId + " is not ACTIVE (Status: " + destAccount.getStatus() + ").");
        }

        // Atomic balance operations
        sourceAccount.debit(amount);
        destAccount.credit(amount);

        accountRepository.save(sourceAccount);
        accountRepository.save(destAccount);

        String txnId = IdGenerator.generateTransactionId();
        String note = (description != null && !description.isBlank()) ? description : "Fund Transfer";

        // Record sender transaction (DEBIT)
        Transaction senderTxn = new Transaction(
                txnId,
                fromAccountId,
                TransactionType.TRANSFER,
                amount,
                note + " -> Transferred to " + toAccountId,
                TransactionStatus.SUCCESS,
                toAccountId
        );
        transactionRepository.save(senderTxn);

        // Record recipient transaction (CREDIT)
        Transaction receiverTxn = new Transaction(
                IdGenerator.generateTransactionId(),
                toAccountId,
                TransactionType.TRANSFER,
                amount,
                note + " -> Received from " + fromAccountId,
                TransactionStatus.SUCCESS,
                fromAccountId
        );
        transactionRepository.save(receiverTxn);

        // Fraud detection evaluation on sender activity
        Customer customer = customerRepository.findById(customerId).orElse(null);
        List<Transaction> recentTransfers = transactionRepository.findRecentTransfers(fromAccountId, Bank.RAPID_TRANSFER_WINDOW_MINUTES);
        FraudContext fraudContext = new FraudContext(customer, sourceAccount, senderTxn, 0, recentTransfers, "Transfer evaluation");
        fraudDetectionService.evaluate(fraudContext);

        return senderTxn;
    }

    /**
     * Retrieves transaction history for an account belonging to the logged-in customer.
     */
    public List<Transaction> getCustomerAccountTransactions(String customerId, String accountId) throws BankingException {
        accountService.validateOwnership(customerId, accountId);
        return transactionRepository.findByAccountId(accountId);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(String transactionId) {
        return transactionRepository.findById(transactionId);
    }
}
