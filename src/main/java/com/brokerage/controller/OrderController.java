package com.brokerage.controller;

import com.brokerage.dto.request.CreateOrderRequest;
import com.brokerage.dto.request.ListOrdersRequest;
import com.brokerage.dto.response.ApiResponse;
import com.brokerage.dto.response.OrderResponse;
import com.brokerage.service.OrderServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderServiceInterface orderService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        
        OrderResponse order = orderService.createOrder(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> listOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        
        List<OrderResponse> orders = orderService.getOrdersForCurrentUser(
                authentication.getName(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        
        orderService.cancelOrder(orderId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Order canceled successfully", null));
    }
}