package com.brokerage.service;

import com.brokerage.dto.request.CustomerRegistrationRequest;
import com.brokerage.dto.response.CustomerResponse;
import com.brokerage.entity.Customer;

public interface CustomerServiceInterface {
    
    CustomerResponse registerCustomer(CustomerRegistrationRequest request);
    
    Customer getCustomerById(Long id);
    
    Customer getCustomerByUsername(String username);
    
    CustomerResponse getCustomerResponseById(Long id);
    
    boolean isAdmin(String username);
}