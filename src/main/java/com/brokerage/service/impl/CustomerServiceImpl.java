package com.brokerage.service.impl;

import com.brokerage.dto.request.CustomerRegistrationRequest;
import com.brokerage.dto.response.CustomerResponse;
import com.brokerage.entity.Customer;
import com.brokerage.exception.BrokerageException;
import com.brokerage.exception.ResourceNotFoundException;
import com.brokerage.mapper.CustomerMapper;
import com.brokerage.repository.CustomerRepository;
import com.brokerage.service.AssetServiceInterface;
import com.brokerage.service.CustomerServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Service
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerServiceInterface {
    
    private final CustomerRepository customerRepository;
    private final AssetServiceInterface assetService;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    
    public CustomerServiceImpl(CustomerRepository customerRepository, 
                              @Lazy AssetServiceInterface assetService,
                              CustomerMapper customerMapper,
                              PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.assetService = assetService;
        this.customerMapper = customerMapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public CustomerResponse registerCustomer(CustomerRegistrationRequest request) {
        if (customerRepository.existsByUsername(request.getUsername())) {
            throw new BrokerageException("Username already exists", HttpStatus.CONFLICT);
        }
        
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BrokerageException("Email already exists", HttpStatus.CONFLICT);
        }
        
        Customer customer = customerMapper.toEntity(request);
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setRoles(Set.of("ROLE_CUSTOMER"));
        customer.setActive(true);
        
        Customer savedCustomer = customerRepository.save(customer);
        
        if (request.getInitialDeposit() != null && request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            assetService.depositToAsset(savedCustomer, "TRY", request.getInitialDeposit());
        }
        
        log.info("Customer registered: {}", savedCustomer.getUsername());
        return customerMapper.toResponse(savedCustomer);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerResponseById(Long id) {
        Customer customer = getCustomerById(id);
        return customerMapper.toResponse(customer);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(String username) {
        Customer customer = getCustomerByUsername(username);
        return customer.getRoles().contains("ROLE_ADMIN");
    }
}