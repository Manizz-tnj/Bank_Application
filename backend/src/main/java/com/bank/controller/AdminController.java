package com.bank.controller;

import com.bank.dto.EmployeeRequest;
import com.bank.dto.EmployeeResponse;
import com.bank.dto.SystemStatsResponse;
import com.bank.entity.Employee;
import com.bank.enums.UserRole;
import com.bank.exception.BankingException;
import com.bank.exception.DuplicateUserException;
import com.bank.mapper.EntityDtoMapper;
import com.bank.repository.EmployeeRepository;
import com.bank.service.BankStatisticsService;
import com.bank.util.IdGenerator;
import com.bank.util.PasswordValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final BankStatisticsService statisticsService;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(BankStatisticsService statisticsService,
                           EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder) {
        this.statisticsService = statisticsService;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/setup-status")
    public ResponseEntity<Map<String, Object>> getSetupStatus() {
        long count = employeeRepository.count();
        return ResponseEntity.ok(Map.of("setupRequired", count == 0));
    }

    @PostMapping("/initial-setup")
    public ResponseEntity<EmployeeResponse> initialSetup(@Valid @RequestBody EmployeeRequest request) {
        PasswordValidator.validatePassword(request.getPassword().trim());

        if (employeeRepository.count() > 0) {
            throw new BankingException("System has already been initialized. An administrator already exists.");
        }
        if (employeeRepository.findByEmail(request.getEmail().trim()).isPresent()) {
            throw new DuplicateUserException("Employee with email " + request.getEmail() + " already exists.");
        }

        String staffId = IdGenerator.generateAdminId();

        Employee admin = new Employee(
                staffId,
                request.getName().trim(),
                request.getEmail().trim(),
                request.getPhone().trim(),
                request.getDepartment() != null && !request.getDepartment().isBlank() ? request.getDepartment().trim() : "Executive Oversight",
                request.getDesignation() != null && !request.getDesignation().isBlank() ? request.getDesignation().trim() : "Chief Administrator",
                UserRole.ADMIN,
                passwordEncoder.encode(request.getPassword().trim())
        );

        Employee saved = employeeRepository.save(admin);
        return new ResponseEntity<>(EntityDtoMapper.toEmployeeResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public ResponseEntity<SystemStatsResponse> getSystemStatistics() {
        SystemStatsResponse stats = statisticsService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        List<EmployeeResponse> list = employeeRepository.findAll().stream()
                .map(EntityDtoMapper::toEmployeeResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/employees")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        PasswordValidator.validatePassword(request.getPassword().trim());

        if (employeeRepository.findByEmail(request.getEmail().trim()).isPresent()) {
            throw new DuplicateUserException("Employee with email " + request.getEmail() + " already exists.");
        }

        String staffId = request.getRole() == UserRole.ADMIN
                ? IdGenerator.generateAdminId()
                : IdGenerator.generateEmployeeId();

        Employee employee = new Employee(
                staffId,
                request.getName().trim(),
                request.getEmail().trim(),
                request.getPhone().trim(),
                request.getDepartment().trim(),
                request.getDesignation().trim(),
                request.getRole(),
                passwordEncoder.encode(request.getPassword().trim())
        );

        Employee saved = employeeRepository.save(employee);
        return new ResponseEntity<>(EntityDtoMapper.toEmployeeResponse(saved), HttpStatus.CREATED);
    }
}
