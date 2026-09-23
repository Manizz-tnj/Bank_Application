package com.bank.interfaces;

import com.bank.exceptions.BankingException;

/**
 * Interface defining contract for fundamental banking transaction operations.
 * Demonstrates method overloading and abstraction.
 */
public interface TransactionOperations {

    /**
     * Deposits money with a default system description.
     * @param amount the positive amount to deposit
     * @throws BankingException if amount is invalid or account is not active
     */
    void deposit(double amount) throws BankingException;

    /**
     * Deposits money with a custom transaction note.
     * @param amount the positive amount to deposit
     * @param description custom note or reference
     * @throws BankingException if amount is invalid or account is not active
     */
    void deposit(double amount, String description) throws BankingException;

    /**
     * Withdraws money with a default system description.
     * @param amount the positive amount to withdraw
     * @throws BankingException if amount is invalid, balance insufficient, or account not active
     */
    void withdraw(double amount) throws BankingException;

    /**
     * Withdraws money with a custom transaction note.
     * @param amount the positive amount to withdraw
     * @param description custom note or reference
     * @throws BankingException if amount is invalid, balance insufficient, or account not active
     */
    void withdraw(double amount, String description) throws BankingException;
}
