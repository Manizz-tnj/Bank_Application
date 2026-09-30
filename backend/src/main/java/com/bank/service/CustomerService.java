package com.bank.service;

import com.bank.dto.CustomerRequest;
import com.bank.dto.CustomerResponse;
import java.util.List;

public interface CustomerService {
    CustomerResponse registerCustomer(CustomerRequest request);
    CustomerResponse getCustomerById(String customerId);
    List<CustomerResponse> getAllCustomers();
    List<CustomerResponse> searchCustomers(String name);
    CustomerResponse updateCustomer(String customerId, CustomerRequest request);
    void changePin(String customerId, String oldPin, String newPin);
    void lockCustomer(String customerId, String reason);
    void unlockCustomer(String customerId);
}
