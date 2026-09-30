package com.bank.repository;

import com.bank.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByAccountIdOrderByTimestampDesc(String accountId);
    List<LedgerEntry> findByAccountIdOrderByTimestampAsc(String accountId);
    List<LedgerEntry> findByTransactionId(String transactionId);

    @Query("SELECT COALESCE(SUM(CASE WHEN l.entryType = 'CREDIT' THEN l.amount ELSE -l.amount END), 0.0) " +
           "FROM LedgerEntry l WHERE l.accountId = :accountId")
    double calculateComputedBalance(@Param("accountId") String accountId);

    default double calculateReconciledBalance(String accountId) {
        return calculateComputedBalance(accountId);
    }
}
