package com.bank.controller;

import com.bank.dto.CustomerRequest;
import com.bank.dto.CustomerResponse;
import com.bank.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable String customerId) {
        CustomerResponse response = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(@RequestParam(required = false) String name) {
        List<CustomerResponse> response = (name != null && !name.isBlank())
                ? customerService.searchCustomers(name)
                : customerService.getAllCustomers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable String customerId,
                                                           @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{customerId}/pin")
    public ResponseEntity<Map<String, String>> changePin(@PathVariable String customerId,
                                                         @RequestBody Map<String, String> payload) {
        String oldPin = payload.get("oldPin");
        String newPin = payload.get("newPin");
        customerService.changePin(customerId, oldPin, newPin);
        return ResponseEntity.ok(Map.of("message", "PIN updated successfully"));
    }

    @PutMapping("/{customerId}/lock")
    public ResponseEntity<Map<String, String>> lockCustomer(@PathVariable String customerId,
                                                            @RequestBody(required = false) Map<String, String> payload) {
        String reason = (payload != null && payload.containsKey("reason"))
                ? payload.get("reason") : "Administrative Lock";
        customerService.lockCustomer(customerId, reason);
        return ResponseEntity.ok(Map.of("message", "Customer " + customerId + " locked"));
    }

    @PutMapping("/{customerId}/unlock")
    public ResponseEntity<Map<String, String>> unlockCustomer(@PathVariable String customerId) {
        customerService.unlockCustomer(customerId);
        return ResponseEntity.ok(Map.of("message", "Customer " + customerId + " unlocked"));
    }
}
