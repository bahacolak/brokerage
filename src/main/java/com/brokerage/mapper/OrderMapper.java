package com.brokerage.mapper;

import com.brokerage.dto.request.CreateOrderRequest;
import com.brokerage.dto.response.OrderResponse;
import com.brokerage.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(source = "side", target = "orderSide")
    Order toEntity(CreateOrderRequest request);
    
    @Mapping(source = "customer.id", target = "customerId")
    OrderResponse toResponse(Order order);
    
    List<OrderResponse> toResponseList(List<Order> orders);
}