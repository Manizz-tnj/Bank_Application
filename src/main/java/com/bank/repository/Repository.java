package com.bank.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface defining standard CRUD operations.
 * Demonstrates Generics and Abstraction in data access layer.
 *
 * @param <T>  Entity type
 * @param <ID> Unique identifier type
 */
public interface Repository<T, ID> {

    /**
     * Persists an entity (creates new or updates existing).
     */
    T save(T entity);

    /**
     * Locates an entity by its unique identifier.
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all entities currently in storage.
     */
    List<T> findAll();

    /**
     * Removes an entity by its identifier.
     */
    boolean deleteById(ID id);

    /**
     * Checks if an entity with the given identifier exists.
     */
    boolean existsById(ID id);

    /**
     * Returns total count of entities.
     */
    long count();

    /**
     * Forces immediate persistence of in-memory data to backing store.
     */
    void flush();
}
