package com.bank.service.impl;

import com.bank.dto.TransactionResponse;
import com.bank.dto.TransferRequest;
import com.bank.entity.Account;
import com.bank.entity.IdempotencyRecord;
import com.bank.entity.LedgerEntry;
import com.bank.entity.Transaction;
import com.bank.enums.AccountStatus;
import com.bank.enums.LedgerEntryType;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.BankingException;
import com.bank.exception.InvalidTransferException;
import com.bank.mapper.EntityDtoMapper;
import com.bank.repository.AccountRepository;
import com.bank.repository.IdempotencyRecordRepository;
import com.bank.repository.LedgerEntryRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.FraudDetectionService;
import com.bank.service.TransactionService;
import com.bank.util.IdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final FraudDetectionService fraudDetectionService;
    private final ObjectMapper objectMapper;

    public TransactionServiceImpl(AccountRepository accountRepository,
                                  TransactionRepository transactionRepository,
                                  LedgerEntryRepository ledgerEntryRepository,
                                  IdempotencyRecordRepository idempotencyRecordRepository,
                                  FraudDetectionService fraudDetectionService,
                                  ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.objectMapper = objectMapper;
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                ? auth.getName() : "SYSTEM";
    }

    @Override
    public TransactionResponse transfer(TransferRequest request) {
        return transfer(request, request.getIdempotencyKey());
    }

    @Override
    public TransactionResponse transfer(TransferRequest request, String idempotencyKey) {
        // 1. Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyRecord> existing = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey.trim());
            if (existing.isPresent()) {
                log.info("Idempotent replay detected for key: {}", idempotencyKey);
                try {
                    return objectMapper.readValue(existing.get().getResponsePayload(), TransactionResponse.class);
                } catch (Exception e) {
                    log.error("Failed to parse cached idempotent response", e);
                }
            }
        }

        if (request.getFromAccountId().equalsIgnoreCase(request.getToAccountId())) {
            throw new InvalidTransferException("Cannot transfer to the same account: " + request.getFromAccountId());
        }

        // 2. Deadlock-Free Pessimistic Locking: Sort account IDs to always lock in consistent order
        String firstId = request.getFromAccountId().compareTo(request.getToAccountId()) < 0
                ? request.getFromAccountId() : request.getToAccountId();
        String secondId = request.getFromAccountId().compareTo(request.getToAccountId()) < 0
                ? request.getToAccountId() : request.getFromAccountId();

        Account firstAccount = accountRepository.findByAccountIdWithLock(firstId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + firstId + " not found"));
        Account secondAccount = accountRepository.findByAccountIdWithLock(secondId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + secondId + " not found"));

        Account source = firstAccount.getAccountId().equalsIgnoreCase(request.getFromAccountId()) ? firstAccount : secondAccount;
        Account destination = firstAccount.getAccountId().equalsIgnoreCase(request.getToAccountId()) ? firstAccount : secondAccount;

        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransferException("Source account is not ACTIVE (Status: " + source.getStatus() + ")");
        }
        if (destination.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransferException("Destination account is not ACTIVE (Status: " + destination.getStatus() + ")");
        }

        // 3. Atomically update balances
        source.withdraw(request.getAmount());
        destination.deposit(request.getAmount());

        accountRepository.save(source);
        accountRepository.save(destination);

        String note = (request.getDescription() != null && !request.getDescription().isBlank())
                ? request.getDescription() : "Fund Transfer";

        // 4. Create Transaction record for source account (DEBIT)
        Transaction sourceTxn = new Transaction(
                IdGenerator.generateTransactionId(),
                source,
                TransactionType.TRANSFER,
                request.getAmount(),
                note + " -> Transferred to " + destination.getAccountId(),
                TransactionStatus.SUCCESS,
                destination.getAccountId()
        );
        Transaction savedSourceTxn = transactionRepository.save(sourceTxn);

        // 5. Create Transaction record for destination account (CREDIT)
        Transaction destTxn = new Transaction(
                IdGenerator.generateTransactionId(),
                destination,
                TransactionType.TRANSFER,
                request.getAmount(),
                note + " -> Received from " + source.getAccountId(),
                TransactionStatus.SUCCESS,
                source.getAccountId()
        );
        Transaction savedDestTxn = transactionRepository.save(destTxn);

        // 6. Double-Entry Immutable Ledger: Record both legs atomically
        String currentUser = getCurrentUser();
        LedgerEntry sourceLedger = new LedgerEntry(
                IdGenerator.generateLedgerId(),
                savedSourceTxn.getTransactionId(),
                source.getAccountId(),
                LedgerEntryType.DEBIT,
                request.getAmount(),
                source.getBalance(),
                savedSourceTxn.getDescription(),
                currentUser
        );
        ledgerEntryRepository.save(sourceLedger);

        LedgerEntry destLedger = new LedgerEntry(
                IdGenerator.generateLedgerId(),
                savedDestTxn.getTransactionId(),
                destination.getAccountId(),
                LedgerEntryType.CREDIT,
                request.getAmount(),
                destination.getBalance(),
                savedDestTxn.getDescription(),
                currentUser
        );
        ledgerEntryRepository.save(destLedger);

        // 7. Fraud Evaluation
        fraudDetectionService.evaluateLargeTransaction(source, savedSourceTxn);
        fraudDetectionService.evaluateTransferVelocity(source);

        TransactionResponse response = EntityDtoMapper.toTransactionResponse(savedSourceTxn);

        // 8. Cache idempotent response
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            try {
                String payload = objectMapper.writeValueAsString(response);
                IdempotencyRecord record = new IdempotencyRecord(
                        idempotencyKey.trim(),
                        "/api/transactions/transfer",
                        null,
                        payload,
                        200
                );
                idempotencyRecordRepository.save(record);
            } catch (Exception e) {
                log.warn("Failed to cache response in IdempotencyRecord", e);
            }
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(String transactionId) {
        Transaction txn = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new BankingException("Transaction " + transactionId + " not found"));
        return EntityDtoMapper.toTransactionResponse(txn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccountId(String accountId) {
        return transactionRepository.findByAccountAccountIdOrderByTimestampDesc(accountId).stream()
                .map(EntityDtoMapper::toTransactionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAllByOrderByTimestampDesc().stream()
                .map(EntityDtoMapper::toTransactionResponse)
                .toList();
    }
}
