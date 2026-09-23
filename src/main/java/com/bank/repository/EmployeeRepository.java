package com.bank.repository;

import com.bank.enums.UserRole;
import com.bank.model.Admin;
import com.bank.model.Employee;
import com.bank.model.Person;
import com.bank.util.HashUtil;
import com.bank.util.IdGenerator;

import java.util.List;
import java.util.Optional;

/**
 * Repository managing internal Bank Staff (Employees and Administrators).
 * Demonstrates Polymorphism and Persistence.
 */
public class EmployeeRepository extends AbstractFileRepository<Person, String> {

    public static final String FILE_PATH = "data/staff.dat";

    public EmployeeRepository() {
        super(FILE_PATH);
        seedDefaultStaffIfEmpty();
        synchronizeSequence();
    }

    public EmployeeRepository(String customPath) {
        super(customPath);
        seedDefaultStaffIfEmpty();
        synchronizeSequence();
    }

    @Override
    protected String extractId(Person entity) {
        return entity.getId();
    }

    private void synchronizeSequence() {
        findAll().stream()
                .map(Person::getId)
                .forEach(id -> {
                    if (id != null && id.startsWith("EMP-")) {
                        try {
                            IdGenerator.synchronizeEmployeeSeq(Long.parseLong(id.substring(4)));
                        } catch (NumberFormatException ignored) {}
                    } else if (id != null && id.startsWith("ADM-")) {
                        try {
                            IdGenerator.synchronizeEmployeeSeq(Long.parseLong(id.substring(4)));
                        } catch (NumberFormatException ignored) {}
                    }
                });
    }

    /**
     * Seeds initial Admin and Employee credentials if the database is newly initialized.
     * Default Admin: ID "ADM-101", PIN "1234"
     * Default Employee: ID "EMP-2001", PIN "1234"
     */
    private void seedDefaultStaffIfEmpty() {
        if (count() == 0) {
            String adminSalt = HashUtil.generateSalt();
            String adminHash = HashUtil.hashPin("1234", adminSalt);
            Admin defaultAdmin = new Admin("ADM-101", "Chief Administrator", "admin@apexbank.com",
                    "18005550001", "Bank Headquarters, Suite 100", 3, adminHash, adminSalt);
            save(defaultAdmin);

            String empSalt = HashUtil.generateSalt();
            String empHash = HashUtil.hashPin("1234", empSalt);
            Employee defaultEmp = new Employee("EMP-2001", "Alice Vance", "alice.vance@apexbank.com",
                    "18005550002", "Branch 01 Operations", "Retail Banking", "Branch Supervisor", empHash, empSalt);
            save(defaultEmp);
        }
    }

    public List<Employee> findAllEmployees() {
        return findAll().stream()
                .filter(p -> p.getRole() == UserRole.EMPLOYEE)
                .filter(p -> p instanceof Employee)
                .map(p -> (Employee) p)
                .toList();
    }

    public List<Admin> findAllAdmins() {
        return findAll().stream()
                .filter(p -> p.getRole() == UserRole.ADMIN)
                .filter(p -> p instanceof Admin)
                .map(p -> (Admin) p)
                .toList();
    }

    public Optional<Person> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return findAll().stream()
                .filter(p -> email.equalsIgnoreCase(p.getEmail()))
                .findFirst();
    }
}
