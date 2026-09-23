package com.bank.repository;

import com.bank.enums.TransactionType;
import com.bank.model.Transaction;
import com.bank.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Repository managing Transaction history, sorting, and velocity/fraud inspection queries.
 * Demonstrates Collections, Generics, and modern Java Streams.
 */
public class TransactionRepository extends AbstractFileRepository<Transaction, String> {

    public static final String FILE_PATH = "data/transactions.dat";

    public TransactionRepository() {
        super(FILE_PATH);
        synchronizeSequence();
    }

    public TransactionRepository(String customPath) {
        super(customPath);
        synchronizeSequence();
    }

    @Override
    protected String extractId(Transaction entity) {
        return entity.getTransactionId();
    }

    private void synchronizeSequence() {
        findAll().stream()
                .map(Transaction::getTransactionId)
                .filter(id -> id != null && id.startsWith("TXN-"))
                .mapToLong(id -> {
                    try {
                        return Long.parseLong(id.substring(4));
                    } catch (NumberFormatException e) {
                        return 0L;
                    }
                })
                .max()
                .ifPresent(IdGenerator::synchronizeTxnSeq);
    }

    public List<Transaction> findByAccountId(String accountId) {
        if (accountId == null) return List.of();
        return findAll().stream()
                .filter(t -> accountId.equals(t.getAccountId()) || accountId.equals(t.getReferenceId()))
                .sorted(Comparator.comparing(Transaction::getDateTime).reversed())
                .toList();
    }

    public List<Transaction> findRecentByAccountId(String accountId, int limit) {
        return findByAccountId(accountId).stream()
                .limit(limit)
                .toList();
    }

    public List<Transaction> findLargeTransactions(double threshold) {
        return findAll().stream()
                .filter(t -> t.getAmount() >= threshold)
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .toList();
    }

    /**
     * Finds transfers initiated by an account within the specified past minutes window.
     */
    public List<Transaction> findRecentTransfers(String accountId, int minutesWindow) {
        if (accountId == null) return List.of();
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutesWindow);
        return findAll().stream()
                .filter(t -> accountId.equals(t.getAccountId()))
                .filter(t -> t.getTransactionType() == TransactionType.TRANSFER)
                .filter(t -> t.getDateTime().isAfter(cutoff))
                .toList();
    }
}
