package com.brokerage.controller;

import com.brokerage.dto.response.ApiResponse;
import com.brokerage.dto.response.AssetResponse;
import com.brokerage.entity.Customer;
import com.brokerage.service.AssetServiceInterface;
import com.brokerage.service.CustomerServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {
    
    private final AssetServiceInterface assetService;
    private final CustomerServiceInterface customerService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<AssetResponse>>> listAssets(
            Authentication authentication) {
        
        Customer customer = customerService.getCustomerByUsername(authentication.getName());
        List<AssetResponse> assets = assetService.getCustomerAssets(customer);
        return ResponseEntity.ok(ApiResponse.success(assets));
    }
}