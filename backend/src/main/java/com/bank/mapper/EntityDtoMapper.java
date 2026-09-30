package com.bank.mapper;

import com.bank.dto.*;
import com.bank.entity.*;

import java.util.Collections;
import java.util.List;

public final class EntityDtoMapper {

    private EntityDtoMapper() {
    }

    public static CustomerResponse toCustomerResponse(Customer customer) {
        if (customer == null) return null;
        List<String> accountIds = customer.getAccounts() != null
                ? customer.getAccounts().stream().map(Account::getAccountId).toList()
                : Collections.emptyList();

        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getDob(),
                customer.getGovernmentId(),
                customer.getKycStatus(),
                customer.getBranchName(),
                customer.getRoutingNumber(),
                customer.getRole(),
                customer.isLocked(),
                customer.getLockReason(),
                customer.getCreatedAt(),
                accountIds
        );
    }

    public static AccountResponse toAccountResponse(Account account) {
        if (account == null) return null;
        String custId = account.getCustomer() != null ? account.getCustomer().getCustomerId() : null;
        String custName = account.getCustomer() != null ? account.getCustomer().getName() : null;

        return new AccountResponse(
                account.getId(),
                account.getAccountId(),
                custId,
                custName,
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getMinimumBalance(),
                account.getInterestRate(),
                account.getOverdraftLimit(),
                account.getTermMonths(),
                account.getMaturityDate(),
                account.calculateInterest(),
                account.getLockReason(),
                account.getCreatedAt()
        );
    }

    public static TransactionResponse toTransactionResponse(Transaction transaction) {
        if (transaction == null) return null;
        String accId = transaction.getAccount() != null ? transaction.getAccount().getAccountId() : null;

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionId(),
                accId,
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getTimestamp(),
                transaction.getDescription(),
                transaction.getReferenceAccountId()
        );
    }

    public static SecurityAlertResponse toSecurityAlertResponse(SecurityAlert alert) {
        if (alert == null) return null;

        return new SecurityAlertResponse(
                alert.getId(),
                alert.getAlertId(),
                alert.getCustomerId(),
                alert.getAccountId(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getRuleTriggered(),
                alert.getDescription(),
                alert.getTimestamp(),
                alert.getResolutionNotes()
        );
    }

    public static EmployeeResponse toEmployeeResponse(Employee employee) {
        if (employee == null) return null;

        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeId(),
                employee.getName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getRole(),
                employee.isLocked(),
                employee.getCreatedAt()
        );
    }
}
