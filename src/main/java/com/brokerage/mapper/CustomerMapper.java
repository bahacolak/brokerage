package com.brokerage.mapper;

import com.brokerage.dto.request.CustomerRegistrationRequest;
import com.brokerage.dto.response.CustomerResponse;
import com.brokerage.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "assets", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Customer toEntity(CustomerRegistrationRequest request);
    
    CustomerResponse toResponse(Customer customer);
    
    List<CustomerResponse> toResponseList(List<Customer> customers);
}