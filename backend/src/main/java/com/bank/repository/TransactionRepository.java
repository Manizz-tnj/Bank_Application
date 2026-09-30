package com.bank.repository;

import com.bank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findByAccountAccountIdOrderByTimestampDesc(String accountId);

    @Query("SELECT t FROM Transaction t WHERE t.account.accountId = :accountId AND t.transactionType = 'TRANSFER' AND t.timestamp >= :since")
    List<Transaction> findRecentTransfersByAccountId(@Param("accountId") String accountId, @Param("since") LocalDateTime since);

    List<Transaction> findAllByOrderByTimestampDesc();
}
