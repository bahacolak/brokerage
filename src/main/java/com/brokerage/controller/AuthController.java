package com.brokerage.controller;

import com.brokerage.dto.request.CustomerRegistrationRequest;
import com.brokerage.dto.request.LoginRequest;
import com.brokerage.dto.response.ApiResponse;
import com.brokerage.dto.response.CustomerResponse;
import com.brokerage.dto.response.JwtAuthResponse;
import com.brokerage.service.AuthService;
import com.brokerage.service.CustomerServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final CustomerServiceInterface customerService;
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerResponse>> register(
            @Valid @RequestBody CustomerRegistrationRequest request) {
        
        CustomerResponse customer = customerService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer registered successfully", customer));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        JwtAuthResponse authResponse = authService.authenticateUser(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }
}