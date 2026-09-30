package com.bank.controller;

import com.bank.dto.AccountRequest;
import com.bank.dto.AccountResponse;
import com.bank.dto.DepositWithdrawRequest;
import com.bank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable String accountId) {
        AccountResponse response = accountService.getAccountById(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getAccountsForCustomer(@PathVariable String customerId) {
        List<AccountResponse> response = accountService.getAccountsForCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> response = accountService.getAllAccounts();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<AccountResponse> deposit(@PathVariable String accountId,
                                                   @Valid @RequestBody DepositWithdrawRequest request) {
        AccountResponse response = accountService.deposit(accountId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable String accountId,
                                                    @Valid @RequestBody DepositWithdrawRequest request) {
        AccountResponse response = accountService.withdraw(accountId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/close")
    public ResponseEntity<Map<String, String>> closeAccount(@PathVariable String accountId) {
        accountService.closeAccount(accountId);
        return ResponseEntity.ok(Map.of("message", "Account " + accountId + " closed successfully"));
    }

    @PutMapping("/{accountId}/lock")
    public ResponseEntity<Map<String, String>> lockAccount(@PathVariable String accountId,
                                                           @RequestBody(required = false) Map<String, String> payload) {
        String reason = (payload != null && payload.containsKey("reason"))
                ? payload.get("reason") : "Administrative Lock";
        accountService.lockAccount(accountId, reason);
        return ResponseEntity.ok(Map.of("message", "Account " + accountId + " locked"));
    }

    @PutMapping("/{accountId}/unlock")
    public ResponseEntity<Map<String, String>> unlockAccount(@PathVariable String accountId) {
        accountService.unlockAccount(accountId);
        return ResponseEntity.ok(Map.of("message", "Account " + accountId + " unlocked"));
    }
}
