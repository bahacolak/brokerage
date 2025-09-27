package com.brokerage.service.impl;

import com.brokerage.dto.request.LoginRequest;
import com.brokerage.dto.response.JwtAuthResponse;
import com.brokerage.entity.Customer;
import com.brokerage.security.JwtProperties;
import com.brokerage.security.JwtTokenProvider;
import com.brokerage.service.AuthService;
import com.brokerage.service.CustomerServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CustomerServiceInterface customerService;
    private final JwtProperties jwtProperties;
    
    @Override
    public JwtAuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        Customer customer = customerService.getCustomerByUsername(loginRequest.getUsername());
        
        return JwtAuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpirationMs())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .roles(customer.getRoles())
                .build();
    }
}