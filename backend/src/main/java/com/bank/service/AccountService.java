package com.bank.service;

import com.bank.dto.AccountRequest;
import com.bank.dto.AccountResponse;
import com.bank.dto.DepositWithdrawRequest;
import java.util.List;

public interface AccountService {
    AccountResponse createAccount(AccountRequest request);
    AccountResponse getAccountById(String accountId);
    List<AccountResponse> getAccountsForCustomer(String customerId);
    List<AccountResponse> getAllAccounts();
    AccountResponse deposit(String accountId, DepositWithdrawRequest request);
    AccountResponse withdraw(String accountId, DepositWithdrawRequest request);
    void closeAccount(String accountId);
    void lockAccount(String accountId, String reason);
    void unlockAccount(String accountId);
}
