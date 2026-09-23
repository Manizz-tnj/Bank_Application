package com.bank.repository;

import com.bank.model.Customer;
import com.bank.util.IdGenerator;

import java.util.List;
import java.util.Optional;

/**
 * Repository managing Customer persistence, lookups, and name-based search queries.
 * Demonstrates Generics, Collections, and Streams.
 */
public class CustomerRepository extends AbstractFileRepository<Customer, String> {

    public static final String FILE_PATH = "data/customers.dat";

    public CustomerRepository() {
        super(FILE_PATH);
        synchronizeSequence();
    }

    public CustomerRepository(String customPath) {
        super(customPath);
        synchronizeSequence();
    }

    @Override
    protected String extractId(Customer entity) {
        return entity.getId();
    }

    private void synchronizeSequence() {
        findAll().stream()
                .map(Customer::getId)
                .filter(id -> id != null && id.startsWith("CUST-"))
                .mapToLong(id -> {
                    try {
                        return Long.parseLong(id.substring(5));
                    } catch (NumberFormatException e) {
                        return 0L;
                    }
                })
                .max()
                .ifPresent(IdGenerator::synchronizeCustomerSeq);
    }

    public Optional<Customer> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return findAll().stream()
                .filter(c -> email.equalsIgnoreCase(c.getEmail()))
                .findFirst();
    }

    public Optional<Customer> findByPhone(String phone) {
        if (phone == null) return Optional.empty();
        return findAll().stream()
                .filter(c -> phone.equals(c.getPhone()))
                .findFirst();
    }

    public List<Customer> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String lower = keyword.trim().toLowerCase();
        return findAll().stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(lower))
                .toList();
    }

    public List<Customer> findLockedCustomers() {
        return findAll().stream()
                .filter(Customer::isLocked)
                .toList();
    }
}
