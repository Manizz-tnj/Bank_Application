package com.bank.repository;

import com.bank.enums.AccountStatus;
import com.bank.enums.AccountType;
import com.bank.model.Account;
import com.bank.util.IdGenerator;

import java.util.List;

/**
 * Repository managing Account persistence and specialized queries.
 * Demonstrates Generics, Polymorphic entity storage, and Streams.
 */
public class AccountRepository extends AbstractFileRepository<Account, String> {

    public static final String FILE_PATH = "data/accounts.dat";

    public AccountRepository() {
        super(FILE_PATH);
        synchronizeSequence();
    }

    public AccountRepository(String customPath) {
        super(customPath);
        synchronizeSequence();
    }

    @Override
    protected String extractId(Account entity) {
        return entity.getAccountId();
    }

    private void synchronizeSequence() {
        findAll().stream()
                .map(Account::getAccountId)
                .filter(id -> id != null && id.contains("-"))
                .mapToLong(id -> {
                    try {
                        String[] parts = id.split("-");
                        return Long.parseLong(parts[parts.length - 1]);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        return 0L;
                    }
                })
                .max()
                .ifPresent(IdGenerator::synchronizeAccountSeq);
    }

    public List<Account> findByCustomerId(String customerId) {
        if (customerId == null) return List.of();
        return findAll().stream()
                .filter(a -> customerId.equals(a.getCustomerId()))
                .toList();
    }

    public List<Account> findByStatus(AccountStatus status) {
        if (status == null) return List.of();
        return findAll().stream()
                .filter(a -> a.getStatus() == status)
                .toList();
    }

    public List<Account> findByType(AccountType type) {
        if (type == null) return List.of();
        return findAll().stream()
                .filter(a -> a.getAccountType() == type)
                .toList();
    }

    public double calculateTotalBankBalance() {
        return findAll().stream()
                .filter(Account::isActive)
                .mapToDouble(Account::getBalance)
                .sum();
    }

    public double calculateCustomerTotalBalance(String customerId) {
        return findByCustomerId(customerId).stream()
                .filter(Account::isActive)
                .mapToDouble(Account::getBalance)
                .sum();
    }
}
