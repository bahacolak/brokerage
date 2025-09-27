package com.brokerage.service;

import com.brokerage.dto.request.LoginRequest;
import com.brokerage.dto.response.JwtAuthResponse;

public interface AuthService {
    
    JwtAuthResponse authenticateUser(LoginRequest loginRequest);
}