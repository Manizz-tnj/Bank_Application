package com.bank.ui;

import com.bank.enums.AlertSeverity;
import com.bank.exceptions.BankingException;
import com.bank.model.*;
import com.bank.security.SecurityService;
import com.bank.service.AccountService;
import com.bank.service.CustomerService;
import com.bank.service.TransactionService;

import java.util.List;

/**
 * Interactive Console Dashboard for Bank Employees.
 */
public class EmployeeMenu {

    private final Employee employee;
    private final CustomerService customerService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final SecurityService securityService;
    private final InputReader reader;

    public EmployeeMenu(Employee employee,
                        CustomerService customerService,
                        AccountService accountService,
                        TransactionService transactionService,
                        SecurityService securityService,
                        InputReader reader) {
        this.employee = employee;
        this.customerService = customerService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.securityService = securityService;
        this.reader = reader;
    }

    public void run() {
        boolean active = true;
        while (active && reader.hasNext()) {
            ConsoleHelper.printHeader("EMPLOYEE DASHBOARD: " + employee.getName() + " (" + employee.getId() + ")");
            System.out.println("Role: " + employee.getRoleSpecificInfo());
            ConsoleHelper.printDivider();
            System.out.println("1. View Customers");
            System.out.println("2. Search Customer (by ID or Name)");
            System.out.println("3. View Accounts");
            System.out.println("4. Search Account (by Account ID)");
            System.out.println("5. View Transactions");
            System.out.println("6. View Security Alerts");
            System.out.println("7. Lock Account");
            System.out.println("8. Unlock Account");
            System.out.println("9. Logout");
            ConsoleHelper.printDivider();

            int choice = reader.readInt("Select Option", 1, 9);
            try {
                switch (choice) {
                    case 1 -> viewCustomers();
                    case 2 -> searchCustomer();
                    case 3 -> viewAccounts();
                    case 4 -> searchAccount();
                    case 5 -> viewTransactions();
                    case 6 -> viewSecurityAlerts();
                    case 7 -> lockAccount();
                    case 8 -> unlockAccount();
                    case 9 -> {
                        ConsoleHelper.printInfo("Logging out of employee dashboard.");
                        active = false;
                    }
                }
            } catch (BankingException be) {
                ConsoleHelper.printError(be.getMessage());
            } catch (Exception e) {
                ConsoleHelper.printError("Unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private void viewCustomers() {
        ConsoleHelper.printSubHeader("ALL REGISTERED CUSTOMERS");
        List<Customer> customers = customerService.getAllCustomers();
        if (customers.isEmpty()) {
            ConsoleHelper.printInfo("No customers registered in database.");
            return;
        }
        for (Customer c : customers) {
            System.out.printf("ID: %-10s | Name: %-20s | Email: %-25s | Status: %s%n",
                    c.getId(), c.getName(), c.getEmail(), (c.isLocked() ? "LOCKED" : "ACTIVE"));
        }
    }

    private void searchCustomer() {
        ConsoleHelper.printSubHeader("SEARCH CUSTOMER");
        System.out.println("1. Search by Customer ID");
        System.out.println("2. Search by Name (Substring)");
        int sub = reader.readInt("Choice", 1, 2);

        if (sub == 1) {
            String id = reader.readString("Enter Customer ID");
            try {
                Customer c = customerService.getCustomerById(id.trim());
                c.displayDetails();
            } catch (BankingException e) {
                ConsoleHelper.printError(e.getMessage());
            }
        } else {
            String keyword = reader.readString("Enter Name Keyword");
            List<Customer> matches = customerService.searchCustomersByName(keyword);
            if (matches.isEmpty()) {
                ConsoleHelper.printInfo("No matching customers found for: " + keyword);
            } else {
                for (Customer c : matches) {
                    c.displayDetails();
                }
            }
        }
    }

    private void viewAccounts() {
        ConsoleHelper.printSubHeader("ALL BANK ACCOUNTS");
        List<Account> accounts = accountService.getAllAccounts();
        if (accounts.isEmpty()) {
            ConsoleHelper.printInfo("No accounts found.");
            return;
        }
        for (Account a : accounts) {
            System.out.printf("ACC: %-14s | Cust: %-10s | %-13s | Bal: $%,11.2f | Status: %s%n",
                    a.getAccountId(), a.getCustomerId(), a.getAccountType(), a.getBalance(), a.getStatus());
        }
    }

    private void searchAccount() {
        ConsoleHelper.printSubHeader("SEARCH ACCOUNT");
        String accId = reader.readString("Enter Account ID");
        try {
            Account acc = accountService.getAccountById(accId.trim());
            acc.displayAccountDetails();
        } catch (BankingException e) {
            ConsoleHelper.printError(e.getMessage());
        }
    }

    private void viewTransactions() {
        ConsoleHelper.printSubHeader("ALL FINANCIAL TRANSACTIONS");
        List<Transaction> txns = transactionService.getAllTransactions();
        if (txns.isEmpty()) {
            ConsoleHelper.printInfo("No transactions recorded.");
            return;
        }
        System.out.printf("%-14s | %-19s | %-14s | %-10s | %-12s | %s%n",
                "Txn ID", "Timestamp", "Account ID", "Type", "Amount", "Description");
        ConsoleHelper.printDivider();
        for (Transaction t : txns) {
            System.out.printf("%-14s | %-19s | %-14s | %-10s | $%,11.2f | %s%n",
                    t.getTransactionId(), t.getFormattedDateTime(), t.getAccountId(),
                    t.getTransactionType(), t.getAmount(), t.getDescription());
        }
    }

    private void viewSecurityAlerts() {
        ConsoleHelper.printSubHeader("SYSTEM SECURITY & FRAUD ALERTS");
        List<SecurityAlert> alerts = securityService.getAllAlerts();
        if (alerts.isEmpty()) {
            ConsoleHelper.printInfo("No security alerts logged.");
            return;
        }
        for (SecurityAlert a : alerts) {
            System.out.println(a);
        }
    }

    private void lockAccount() throws BankingException {
        ConsoleHelper.printSubHeader("LOCK ACCOUNT");
        String accId = reader.readString("Enter Account ID to Lock");
        String reason = reader.readString("Enter Reason for Locking");
        securityService.lockAccount(accId.trim(), reason, "Employee " + employee.getId());
        ConsoleHelper.printSuccess("Account " + accId + " has been locked.");
    }

    private void unlockAccount() throws BankingException {
        ConsoleHelper.printSubHeader("UNLOCK ACCOUNT");
        String accId = reader.readString("Enter Account ID to Unlock");
        securityService.unlockAccount(accId.trim(), "Employee " + employee.getId());
        ConsoleHelper.printSuccess("Account " + accId + " has been unlocked.");
    }
}
