package com.brokerage.service;

import com.brokerage.dto.request.CreateOrderRequest;
import com.brokerage.dto.request.CreateOrderForCustomerRequest;
import com.brokerage.dto.request.ListOrdersRequest;
import com.brokerage.dto.response.OrderResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderServiceInterface {
    
    OrderResponse createOrder(CreateOrderRequest request, String username);
    
    OrderResponse createOrderForCustomer(CreateOrderForCustomerRequest request);
    
    void cancelOrder(Long orderId, String username);
    
    void cancelOrderAsAdmin(Long orderId);
    
    void matchOrder(Long orderId);
    
    List<OrderResponse> listOrders(ListOrdersRequest request);
    
    List<OrderResponse> listAllOrders();
    
    List<OrderResponse> getCustomerOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<OrderResponse> getOrdersForCurrentUser(String username, LocalDateTime startDate, LocalDateTime endDate);
}