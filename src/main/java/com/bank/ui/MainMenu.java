package com.bank.ui;

import com.bank.enums.UserRole;
import com.bank.exceptions.BankingException;
import com.bank.model.Admin;
import com.bank.model.Bank;
import com.bank.model.Customer;
import com.bank.model.Employee;
import com.bank.model.Person;
import com.bank.repository.*;
import com.bank.security.FraudDetectionService;
import com.bank.security.SecurityService;
import com.bank.service.*;

/**
 * Top-Level Application Orchestrator and Main Menu Console Router.
 */
public class MainMenu {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SecurityAlertRepository alertRepository;
    private final EmployeeRepository employeeRepository;

    private final SecurityService securityService;
    private final FraudDetectionService fraudDetectionService;
    private final AuthenticationService authService;
    private final CustomerService customerService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final BankStatisticsService statisticsService;
    private final Bank bank;
    private final InputReader reader;

    public MainMenu() {
        this.bank = new Bank();
        this.reader = new InputReader();

        // Repositories (with file persistence in data/)
        this.customerRepository = new CustomerRepository();
        this.accountRepository = new AccountRepository();
        this.transactionRepository = new TransactionRepository();
        this.alertRepository = new SecurityAlertRepository();
        this.employeeRepository = new EmployeeRepository();

        // Security Subsystem
        this.securityService = new SecurityService(alertRepository, accountRepository, customerRepository);
        this.fraudDetectionService = new FraudDetectionService(alertRepository);
        this.authService = new AuthenticationService(customerRepository, employeeRepository, securityService, fraudDetectionService);

        // Core Business Services
        this.customerService = new CustomerService(customerRepository, securityService);
        this.accountService = new AccountService(accountRepository, customerRepository, transactionRepository, securityService);
        this.transactionService = new TransactionService(accountRepository, customerRepository, transactionRepository,
                fraudDetectionService, securityService, accountService);
        this.statisticsService = new BankStatisticsService(customerRepository, accountRepository, transactionRepository, alertRepository);
    }

    public void start() {
        boolean running = true;
        while (running && reader.hasNext()) {
            ConsoleHelper.printHeader(bank.getBankName());
            System.out.println("Branch: " + bank.getBranch() + " | SWIFT: " + bank.getSwiftCode());
            ConsoleHelper.printDivider();
            System.out.println("1. Customer Login");
            System.out.println("2. Employee Login");
            System.out.println("3. Admin Login");
            System.out.println("4. Customer Registration");
            System.out.println("5. Exit Application");
            ConsoleHelper.printDivider();

            int choice = reader.readInt("Select Option", 1, 5);
            try {
                switch (choice) {
                    case 1 -> handleCustomerLogin();
                    case 2 -> handleEmployeeLogin();
                    case 3 -> handleAdminLogin();
                    case 4 -> handleCustomerRegistration();
                    case 5 -> {
                        flushAllData();
                        ConsoleHelper.printInfo("Thank you for using " + bank.getBankName() + ". System shutdown clean.");
                        running = false;
                    }
                }
            } catch (Exception e) {
                ConsoleHelper.printError("Error: " + e.getMessage());
            }
        }
    }

    private void handleCustomerLogin() {
        ConsoleHelper.printSubHeader("CUSTOMER SECURE LOGIN");
        String customerId = reader.readString("Enter Customer ID (e.g. CUST-1001)");
        String pin = reader.readPin("Enter PIN");

        try {
            Customer customer = authService.authenticateCustomer(customerId, pin);
            ConsoleHelper.printSuccess("Welcome, " + customer.getName() + "!");
            CustomerMenu menu = new CustomerMenu(customer, customerService, accountService, transactionService, reader);
            menu.run();
        } catch (BankingException e) {
            ConsoleHelper.printError("Login Failed: " + e.getMessage());
        }
    }

    private void handleEmployeeLogin() {
        ConsoleHelper.printSubHeader("EMPLOYEE SECURE LOGIN");
        System.out.println("(Default Employee Seed: ID 'EMP-2001' | PIN '1234')");
        String staffId = reader.readString("Enter Employee ID");
        String pin = reader.readPin("Enter PIN");

        try {
            Person staff = authService.authenticateStaff(staffId, pin, UserRole.EMPLOYEE);
            ConsoleHelper.printSuccess("Welcome, " + staff.getName() + "!");
            EmployeeMenu menu = new EmployeeMenu((Employee) staff, customerService, accountService,
                    transactionService, securityService, reader);
            menu.run();
        } catch (BankingException e) {
            ConsoleHelper.printError("Login Failed: " + e.getMessage());
        }
    }

    private void handleAdminLogin() {
        ConsoleHelper.printSubHeader("ADMINISTRATOR SECURE LOGIN");
        System.out.println("(Default Admin Seed: ID 'ADM-101' | PIN '1234')");
        String staffId = reader.readString("Enter Admin ID");
        String pin = reader.readPin("Enter PIN");

        try {
            Person staff = authService.authenticateStaff(staffId, pin, UserRole.ADMIN);
            ConsoleHelper.printSuccess("Welcome Administrator, " + staff.getName() + "!");
            AdminMenu menu = new AdminMenu((Admin) staff, customerService, accountService,
                    transactionService, securityService, statisticsService, employeeRepository, reader);
            menu.run();
        } catch (BankingException e) {
            ConsoleHelper.printError("Login Failed: " + e.getMessage());
        }
    }

    private void handleCustomerRegistration() {
        ConsoleHelper.printSubHeader("NEW CUSTOMER REGISTRATION");
        String name = reader.readString("Enter Full Name");
        String email = reader.readString("Enter Email Address");
        String phone = reader.readString("Enter Mobile Phone (Digits only)");
        String address = reader.readString("Enter Residential Address");
        String pin = reader.readPin("Create 4-6 Digit Security PIN");
        String confirmPin = reader.readPin("Confirm Security PIN");

        if (!pin.equals(confirmPin)) {
            ConsoleHelper.printError("PIN entries do not match. Registration cancelled.");
            return;
        }

        try {
            Customer newCustomer = customerService.registerCustomer(name, email, phone, address, pin);
            ConsoleHelper.printSuccess("Customer account registered successfully!");
            System.out.println(">> YOUR CUSTOMER ID : " + newCustomer.getId());
            System.out.println(">> Please save this Customer ID for future logins.");
        } catch (BankingException e) {
            ConsoleHelper.printError("Registration Failed: " + e.getMessage());
        }
    }

    private void flushAllData() {
        customerRepository.flush();
        accountRepository.flush();
        transactionRepository.flush();
        alertRepository.flush();
        employeeRepository.flush();
    }
}
