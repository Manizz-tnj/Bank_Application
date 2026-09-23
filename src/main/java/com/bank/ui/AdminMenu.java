package com.bank.ui;

import com.bank.enums.AlertSeverity;
import com.bank.exceptions.BankingException;
import com.bank.model.*;
import com.bank.repository.EmployeeRepository;
import com.bank.security.SecurityService;
import com.bank.service.AccountService;
import com.bank.service.BankStatisticsService;
import com.bank.service.CustomerService;
import com.bank.service.TransactionService;
import com.bank.util.HashUtil;
import com.bank.util.IdGenerator;
import com.bank.util.ValidationUtil;

import java.util.List;

/**
 * Interactive Console Dashboard for System Administrators.
 */
public class AdminMenu {

    private final Admin admin;
    private final CustomerService customerService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final SecurityService securityService;
    private final BankStatisticsService statisticsService;
    private final EmployeeRepository employeeRepository;
    private final InputReader reader;

    public AdminMenu(Admin admin,
                     CustomerService customerService,
                     AccountService accountService,
                     TransactionService transactionService,
                     SecurityService securityService,
                     BankStatisticsService statisticsService,
                     EmployeeRepository employeeRepository,
                     InputReader reader) {
        this.admin = admin;
        this.customerService = customerService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.securityService = securityService;
        this.statisticsService = statisticsService;
        this.employeeRepository = employeeRepository;
        this.reader = reader;
    }

    public void run() {
        boolean active = true;
        while (active && reader.hasNext()) {
            ConsoleHelper.printHeader("ADMIN DASHBOARD: " + admin.getName() + " (" + admin.getId() + ")");
            System.out.println("Clearance: " + admin.getRoleSpecificInfo());
            ConsoleHelper.printDivider();
            System.out.println("1.  View All Customers");
            System.out.println("2.  View All Bank Staff / Employees");
            System.out.println("3.  View All Accounts");
            System.out.println("4.  View All Transactions");
            System.out.println("5.  View Security Alerts & Incidents");
            System.out.println("6.  Search Records");
            System.out.println("7.  System Statistics & Analytics");
            System.out.println("8.  Lock Account or Customer Profile");
            System.out.println("9.  Unlock Account or Customer Profile");
            System.out.println("10. Manage Employee Accounts (Create New Staff)");
            System.out.println("11. Logout");
            ConsoleHelper.printDivider();

            int choice = reader.readInt("Select Option", 1, 11);
            try {
                switch (choice) {
                    case 1 -> viewCustomers();
                    case 2 -> viewEmployees();
                    case 3 -> viewAccounts();
                    case 4 -> viewTransactions();
                    case 5 -> viewSecurityAlerts();
                    case 6 -> searchRecords();
                    case 7 -> statisticsService.displaySystemStatistics();
                    case 8 -> lockEntity();
                    case 9 -> unlockEntity();
                    case 10 -> manageEmployees();
                    case 11 -> {
                        ConsoleHelper.printInfo("Logging out of Administrator dashboard.");
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
            ConsoleHelper.printInfo("No customers registered.");
            return;
        }
        for (Customer c : customers) {
            System.out.printf("ID: %-10s | Name: %-20s | Email: %-25s | Accounts: %d | Status: %s%n",
                    c.getId(), c.getName(), c.getEmail(), c.getAccountCount(), (c.isLocked() ? "LOCKED" : "ACTIVE"));
        }
    }

    private void viewEmployees() {
        ConsoleHelper.printSubHeader("BANK STAFF DIRECTORY");
        List<Employee> emps = employeeRepository.findAllEmployees();
        for (Employee e : emps) {
            System.out.printf("EMP: %-10s | %-20s | %-18s | %-18s | Status: %s%n",
                    e.getId(), e.getName(), e.getDepartment(), e.getDesignation(), (e.isLocked() ? "LOCKED" : "ACTIVE"));
        }
    }

    private void viewAccounts() {
        ConsoleHelper.printSubHeader("ALL BANK ACCOUNTS");
        List<Account> accounts = accountService.getAllAccounts();
        for (Account a : accounts) {
            System.out.printf("ACC: %-14s | Cust: %-10s | %-13s | Bal: $%,11.2f | Status: %s%n",
                    a.getAccountId(), a.getCustomerId(), a.getAccountType(), a.getBalance(), a.getStatus());
        }
    }

    private void viewTransactions() {
        ConsoleHelper.printSubHeader("GLOBAL TRANSACTION LEDGER");
        List<Transaction> txns = transactionService.getAllTransactions();
        for (Transaction t : txns) {
            System.out.printf("%-14s | %-19s | %-14s | %-10s | $%,11.2f | %s%n",
                    t.getTransactionId(), t.getFormattedDateTime(), t.getAccountId(),
                    t.getTransactionType(), t.getAmount(), t.getDescription());
        }
    }

    private void viewSecurityAlerts() {
        ConsoleHelper.printSubHeader("SECURITY AUDIT INCIDENTS");
        System.out.println("1. View All Alerts");
        System.out.println("2. Filter High / Critical Severity Alerts");
        System.out.println("3. Resolve an Alert");
        int sub = reader.readInt("Choice", 1, 3);

        if (sub == 1) {
            securityService.getAllAlerts().forEach(System.out::println);
        } else if (sub == 2) {
            securityService.getAlertsBySeverity(AlertSeverity.HIGH).forEach(System.out::println);
            securityService.getAlertsBySeverity(AlertSeverity.CRITICAL).forEach(System.out::println);
        } else {
            String alertId = reader.readString("Enter Alert ID to Resolve");
            String notes = reader.readString("Enter Resolution Notes");
            try {
                securityService.resolveAlert(alertId, notes);
                ConsoleHelper.printSuccess("Alert " + alertId + " marked as RESOLVED.");
            } catch (BankingException e) {
                ConsoleHelper.printError(e.getMessage());
            }
        }
    }

    private void searchRecords() {
        ConsoleHelper.printSubHeader("SEARCH SYSTEM RECORDS");
        System.out.println("1. Search Customer by ID or Name");
        System.out.println("2. Search Account by ID");
        System.out.println("3. Search Transaction by ID");
        int sub = reader.readInt("Choice", 1, 3);

        if (sub == 1) {
            String query = reader.readString("Enter Customer ID or Name substring");
            List<Customer> matches = customerService.searchCustomersByName(query);
            if (matches.isEmpty()) {
                try {
                    Customer c = customerService.getCustomerById(query);
                    c.displayDetails();
                } catch (BankingException e) {
                    ConsoleHelper.printInfo("No customer found matching: " + query);
                }
            } else {
                matches.forEach(Customer::displayDetails);
            }
        } else if (sub == 2) {
            String accId = reader.readString("Enter Account ID");
            try {
                Account acc = accountService.getAccountById(accId);
                acc.displayAccountDetails();
            } catch (BankingException e) {
                ConsoleHelper.printError(e.getMessage());
            }
        } else {
            String txnId = reader.readString("Enter Transaction ID");
            transactionService.getTransactionById(txnId)
                    .ifPresentOrElse(
                            System.out::println,
                            () -> ConsoleHelper.printInfo("Transaction not found: " + txnId)
                    );
        }
    }

    private void lockEntity() throws BankingException {
        ConsoleHelper.printSubHeader("SECURITY LOCK ENFORCEMENT");
        System.out.println("1. Lock an Account");
        System.out.println("2. Lock a Customer Profile");
        int sub = reader.readInt("Choice", 1, 2);

        if (sub == 1) {
            String accId = reader.readString("Enter Account ID to Lock");
            String reason = reader.readString("Enter Lock Reason");
            securityService.lockAccount(accId, reason, "Administrator " + admin.getId());
            ConsoleHelper.printSuccess("Account " + accId + " is now LOCKED.");
        } else {
            String custId = reader.readString("Enter Customer ID to Lock");
            String reason = reader.readString("Enter Lock Reason");
            securityService.lockCustomer(custId, reason);
            ConsoleHelper.printSuccess("Customer Profile " + custId + " is now LOCKED.");
        }
    }

    private void unlockEntity() throws BankingException {
        ConsoleHelper.printSubHeader("SECURITY UNLOCK ENFORCEMENT");
        System.out.println("1. Unlock an Account");
        System.out.println("2. Unlock a Customer Profile");
        int sub = reader.readInt("Choice", 1, 2);

        if (sub == 1) {
            String accId = reader.readString("Enter Account ID to Unlock");
            securityService.unlockAccount(accId, "Administrator " + admin.getId());
            ConsoleHelper.printSuccess("Account " + accId + " is now UNLOCKED.");
        } else {
            String custId = reader.readString("Enter Customer ID to Unlock");
            securityService.unlockCustomer(custId);
            ConsoleHelper.printSuccess("Customer Profile " + custId + " is now UNLOCKED.");
        }
    }

    private void manageEmployees() throws BankingException {
        ConsoleHelper.printSubHeader("CREATE NEW EMPLOYEE ACCOUNT");
        String name = reader.readString("Enter Employee Name");
        String email = reader.readString("Enter Employee Email");
        String phone = reader.readString("Enter Phone Number");
        String address = reader.readString("Enter Address");
        String dept = reader.readString("Enter Department (e.g. Retail, Security, Loans)");
        String designation = reader.readString("Enter Designation (e.g. Teller, Officer)");
        String pin = reader.readPin("Assign Initial 4-6 Digit PIN");

        ValidationUtil.validateNonEmpty(name, "Name");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePhone(phone);

        String empId = IdGenerator.generateEmployeeId();
        String salt = HashUtil.generateSalt();
        String hash = HashUtil.hashPin(pin, salt);

        Employee newEmp = new Employee(empId, name, email, phone, address, dept, designation, hash, salt);
        employeeRepository.save(newEmp);

        ConsoleHelper.printSuccess("Employee created successfully! ID: " + empId);
    }
}
