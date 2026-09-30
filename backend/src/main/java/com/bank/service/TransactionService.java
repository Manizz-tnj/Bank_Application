package com.bank.service;

import com.bank.dto.TransactionResponse;
import com.bank.dto.TransferRequest;
import java.util.List;

public interface TransactionService {
    TransactionResponse transfer(TransferRequest request);
    TransactionResponse transfer(TransferRequest request, String idempotencyKey);
    TransactionResponse getTransactionById(String transactionId);
    List<TransactionResponse> getTransactionsByAccountId(String accountId);
    List<TransactionResponse> getAllTransactions();
}
