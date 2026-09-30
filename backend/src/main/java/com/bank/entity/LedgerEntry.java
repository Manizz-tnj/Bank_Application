package com.bank.entity;

import com.bank.enums.LedgerEntryType;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_ledger_acc", columnList = "account_id"),
        @Index(name = "idx_ledger_txn", columnList = "transaction_id")
})
@EntityListeners(AuditingEntityListener.class)
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ledger_id", unique = true, nullable = false, length = 30)
    private String ledgerId;

    @Column(name = "transaction_id", nullable = false, length = 30)
    private String transactionId;

    @Column(name = "account_id", nullable = false, length = 30)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private LedgerEntryType entryType;

    @Column(nullable = false)
    private double amount;

    @Column(name = "balance_after", nullable = false)
    private double balanceAfter;

    @Column(nullable = false, length = 255)
    private String narration;

    @CreatedDate
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @CreatedBy
    @Column(name = "created_by", length = 50)
    private String createdBy;

    public LedgerEntry() {
    }

    public LedgerEntry(String ledgerId, String transactionId, String accountId,
                       LedgerEntryType entryType, double amount, double balanceAfter,
                       String narration) {
        this(ledgerId, transactionId, accountId, entryType, amount, balanceAfter, narration, null);
    }

    public LedgerEntry(String ledgerId, String transactionId, String accountId,
                       LedgerEntryType entryType, double amount, double balanceAfter,
                       String narration, String createdBy) {
        this.ledgerId = ledgerId;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.entryType = entryType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.narration = narration;
        this.createdBy = createdBy;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(String ledgerId) {
        this.ledgerId = ledgerId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(LedgerEntryType entryType) {
        this.entryType = entryType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
