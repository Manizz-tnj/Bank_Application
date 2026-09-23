package com.bank.ui;

import com.bank.enums.AccountType;
import com.bank.exceptions.BankingException;
import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.service.AccountService;
import com.bank.service.CustomerService;
import com.bank.service.TransactionService;

import java.util.List;

/**
 * Interactive Console Dashboard for Bank Customers.
 */
public class CustomerMenu {

    private final Customer customer;
    private final CustomerService customerService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final InputReader reader;

    public CustomerMenu(Customer customer,
                        CustomerService customerService,
                        AccountService accountService,
                        TransactionService transactionService,
                        InputReader reader) {
        this.customer = customer;
        this.customerService = customerService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.reader = reader;
    }

    public void run() {
        boolean active = true;
        while (active && reader.hasNext()) {
            ConsoleHelper.printHeader("CUSTOMER DASHBOARD: " + customer.getName() + " (" + customer.getId() + ")");
            System.out.println("1.  View Profile");
            System.out.println("2.  Create Account (Savings / Current / Fixed Deposit)");
            System.out.println("3.  View My Accounts");
            System.out.println("4.  Deposit Money");
            System.out.println("5.  Withdraw Money");
            System.out.println("6.  Transfer Money");
            System.out.println("7.  Check Balance");
            System.out.println("8.  View Transaction History");
            System.out.println("9.  Change PIN");
            System.out.println("10. Close Account");
            System.out.println("11. Logout");
            ConsoleHelper.printDivider();

            int choice = reader.readInt("Select Option", 1, 11);
            try {
                switch (choice) {
                    case 1 -> viewProfile();
                    case 2 -> createAccount();
                    case 3 -> viewMyAccounts();
                    case 4 -> handleDeposit();
                    case 5 -> handleWithdrawal();
                    case 6 -> handleTransfer();
                    case 7 -> checkBalance();
                    case 8 -> viewTransactionHistory();
                    case 9 -> changePin();
                    case 10 -> closeAccount();
                    case 11 -> {
                        ConsoleHelper.printInfo("Logging out of customer session. Goodbye!");
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

    private void viewProfile() throws BankingException {
        // Re-fetch latest customer state
        Customer latest = customerService.getCustomerById(customer.getId());
        latest.displayDetails();
    }

    private void createAccount() throws BankingException {
        ConsoleHelper.printSubHeader("OPEN A NEW BANK ACCOUNT");
        System.out.println("1. Savings Account (Min Bal: $1,000 | 4.0% p.a. interest)");
        System.out.println("2. Current Account ($10,000 Overdraft facility)");
        System.out.println("3. Fixed Deposit Account (Term investment | 7.0% p.a. interest)");
        int typeChoice = reader.readInt("Select Account Type", 1, 3);

        switch (typeChoice) {
            case 1 -> {
                double deposit = reader.readPositiveDouble("Enter Initial Deposit (Min $1,000)");
                Account acc = accountService.createSavingsAccount(customer.getId(), deposit);
                ConsoleHelper.printSuccess("Savings Account successfully opened! ID: " + acc.getAccountId());
            }
            case 2 -> {
                double deposit = reader.readPositiveDouble("Enter Initial Deposit (Min $2,000)");
                double overdraft = reader.readPositiveDouble("Enter Desired Overdraft Limit (Default $10,000)");
                Account acc = accountService.createCurrentAccount(customer.getId(), deposit, overdraft);
                ConsoleHelper.printSuccess("Current Account successfully opened! ID: " + acc.getAccountId());
            }
            case 3 -> {
                double deposit = reader.readPositiveDouble("Enter Fixed Deposit Principal Amount (Min $5,000)");
                int months = reader.readInt("Enter Term in Months (e.g. 6, 12, 24, 36)", 3, 120);
                Account acc = accountService.createFixedDepositAccount(customer.getId(), deposit, months);
                ConsoleHelper.printSuccess("Fixed Deposit opened! ID: " + acc.getAccountId());
            }
        }
    }

    private void viewMyAccounts() {
        ConsoleHelper.printSubHeader("MY REGISTERED ACCOUNTS");
        List<Account> accounts = accountService.getAccountsForCustomer(customer.getId());
        if (accounts.isEmpty()) {
            ConsoleHelper.printInfo("You do not have any registered bank accounts yet.");
            return;
        }
        for (Account acc : accounts) {
            acc.displayAccountDetails();
        }
    }

    private void handleDeposit() throws BankingException {
        ConsoleHelper.printSubHeader("DEPOSIT FUNDS");
        String accountId = selectCustomerAccount();
        if (accountId == null) return;

        double amount = reader.readPositiveDouble("Enter Deposit Amount");
        String note = reader.readString("Enter Transaction Note (optional)");

        Transaction txn = transactionService.deposit(customer.getId(), accountId, amount, note);
        ConsoleHelper.printSuccess(String.format("Deposit of $%,.2f completed! Ref: %s", amount, txn.getTransactionId()));
    }

    private void handleWithdrawal() throws BankingException {
        ConsoleHelper.printSubHeader("WITHDRAW FUNDS");
        String accountId = selectCustomerAccount();
        if (accountId == null) return;

        double amount = reader.readPositiveDouble("Enter Withdrawal Amount");
        String note = reader.readString("Enter Purpose / Note (optional)");

        Transaction txn = transactionService.withdraw(customer.getId(), accountId, amount, note);
        ConsoleHelper.printSuccess(String.format("Withdrawal of $%,.2f completed! Ref: %s", amount, txn.getTransactionId()));
    }

    private void handleTransfer() throws BankingException {
        ConsoleHelper.printSubHeader("TRANSFER MONEY");
        String fromAccount = selectCustomerAccount();
        if (fromAccount == null) return;

        String toAccount = reader.readString("Enter Recipient Account ID");
        double amount = reader.readPositiveDouble("Enter Transfer Amount");
        String note = reader.readString("Enter Transfer Description");

        Transaction txn = transactionService.transfer(customer.getId(), fromAccount, toAccount, amount, note);
        ConsoleHelper.printSuccess(String.format("Transfer of $%,.2f to %s completed! Ref: %s",
                amount, toAccount, txn.getTransactionId()));
    }

    private void checkBalance() throws BankingException {
        ConsoleHelper.printSubHeader("CHECK ACCOUNT BALANCE");
        String accountId = selectCustomerAccount();
        if (accountId == null) return;

        Account acc = accountService.validateOwnership(customer.getId(), accountId);
        System.out.printf(">> Account ID      : %s (%s)%n", acc.getAccountId(), acc.getAccountType().getDescription());
        System.out.printf(">> Current Balance : $%,.2f%n", acc.getBalance());
        System.out.printf(">> Status          : %s%n", acc.getStatus());
    }

    private void viewTransactionHistory() throws BankingException {
        ConsoleHelper.printSubHeader("TRANSACTION HISTORY AUDIT");
        String accountId = selectCustomerAccount();
        if (accountId == null) return;

        List<Transaction> history = transactionService.getCustomerAccountTransactions(customer.getId(), accountId);
        if (history.isEmpty()) {
            ConsoleHelper.printInfo("No transactions recorded for account " + accountId);
            return;
        }

        System.out.printf("%-14s | %-19s | %-10s | %-12s | %-10s | %s%n",
                "Transaction ID", "Date & Time", "Type", "Amount", "Status", "Description");
        ConsoleHelper.printDivider();
        for (Transaction t : history) {
            System.out.printf("%-14s | %-19s | %-10s | $%,11.2f | %-10s | %s%n",
                    t.getTransactionId(), t.getFormattedDateTime(), t.getTransactionType(),
                    t.getAmount(), t.getStatus(), t.getDescription());
        }
    }

    private void changePin() throws BankingException {
        ConsoleHelper.printSubHeader("CHANGE SECURITY PIN");
        String oldPin = reader.readPin("Enter Current PIN");
        String newPin = reader.readPin("Enter New 4-6 Digit PIN");
        String confirmPin = reader.readPin("Confirm New 4-6 Digit PIN");

        if (!newPin.equals(confirmPin)) {
            ConsoleHelper.printError("New PIN entries do not match. Operation cancelled.");
            return;
        }

        customerService.changePin(customer.getId(), oldPin, newPin);
        ConsoleHelper.printSuccess("Security PIN updated successfully!");
    }

    private void closeAccount() throws BankingException {
        ConsoleHelper.printSubHeader("CLOSE ACCOUNT");
        String accountId = selectCustomerAccount();
        if (accountId == null) return;

        String confirm = reader.readString("Are you sure you want to permanently close " + accountId + "? (yes/no)");
        if ("yes".equalsIgnoreCase(confirm)) {
            accountService.closeAccount(customer.getId(), accountId);
            ConsoleHelper.printSuccess("Account " + accountId + " has been closed successfully.");
        } else {
            ConsoleHelper.printInfo("Account closure aborted.");
        }
    }

    private String selectCustomerAccount() {
        List<Account> accounts = accountService.getAccountsForCustomer(customer.getId());
        if (accounts.isEmpty()) {
            ConsoleHelper.printError("You have no accounts available. Please open an account first.");
            return null;
        }
        System.out.println("Select Account:");
        for (int i = 0; i < accounts.size(); i++) {
            Account a = accounts.get(i);
            System.out.printf(" %d. %s [%s] - Balance: $%,.2f (%s)%n",
                    (i + 1), a.getAccountId(), a.getAccountType(), a.getBalance(), a.getStatus());
        }
        int sel = reader.readInt("Choice", 1, accounts.size());
        return accounts.get(sel - 1).getAccountId();
    }
}
