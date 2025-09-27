package com.brokerage.controller;

import com.brokerage.dto.request.CreateOrderForCustomerRequest;
import com.brokerage.dto.request.MatchOrderRequest;
import com.brokerage.dto.response.ApiResponse;
import com.brokerage.dto.response.OrderResponse;
import com.brokerage.service.OrderServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final OrderServiceInterface orderService;
    
    @PostMapping("/orders/match")
    public ResponseEntity<ApiResponse<Void>> matchOrder(
            @Valid @RequestBody MatchOrderRequest request) {
        
        orderService.matchOrder(request.getOrderId());
        return ResponseEntity.ok(ApiResponse.success("Order matched successfully", null));
    }
    
    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrderForCustomer(
            @Valid @RequestBody CreateOrderForCustomerRequest request) {
        
        OrderResponse order = orderService.createOrderForCustomer(request);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", order));
    }
    
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId) {
        
        orderService.cancelOrderAsAdmin(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order canceled successfully", null));
    }
    
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> listAllOrders() {
        
        List<OrderResponse> orders = orderService.listAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
}