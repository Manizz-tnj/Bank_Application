package com.bank.config;

import com.bank.entity.Employee;
import com.bank.enums.UserRole;
import com.bank.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Starting Apex National Bank Core Systems in clean-slate mode (Zero dummy customer data)...");

        // Provision/Update Administrator account with secure enterprise password: Admin@Apex2026!
        employeeRepository.findByEmployeeId("ADM-101").ifPresentOrElse(
                admin -> {
                    admin.setPasswordHash(passwordEncoder.encode("Admin@Apex2026!"));
                    admin.setLocked(false);
                    employeeRepository.save(admin);
                    log.info("System Administrator ADM-101 password reset to Admin@Apex2026!");
                },
                () -> {
                    Employee admin = new Employee(
                            "ADM-101",
                            "Chief Administrator",
                            "admin@apexbank.com",
                            "18005550001",
                            "Executive Oversight",
                            "Senior Director",
                            UserRole.ADMIN,
                            passwordEncoder.encode("Admin@Apex2026!")
                    );
                    employeeRepository.save(admin);
                    log.info("System Administrator provisioned: ADM-101 with secure password Admin@Apex2026!");
                }
        );

        // Customer database is 100% clean - Real customers onboard via KYC registration
        log.info("Apex National Bank ready for real-time customer onboarding.");
    }
}
