package com.bank.interfaces;

import com.bank.exceptions.BankingException;

/**
 * Interface defining contract for security and credential management operations.
 */
public interface SecurityOperations {

    /**
     * Updates authentication PIN with validation.
     * @param oldPinHash hashed existing PIN
     * @param newPinHash hashed replacement PIN
     * @throws BankingException if PIN invalid or account locked
     */
    void updatePin(String oldPinHash, String newPinHash) throws BankingException;

    /**
     * Locks the entity for security reasons.
     * @param reason explanation for the security lockout
     */
    void lock(String reason);

    /**
     * Unlocks the entity and restores regular operations.
     */
    void unlock();

    /**
     * Returns true if currently locked.
     */
    boolean isLocked();
}
