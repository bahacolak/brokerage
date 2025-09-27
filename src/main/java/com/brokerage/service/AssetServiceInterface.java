package com.brokerage.service;

import com.brokerage.dto.response.AssetResponse;
import com.brokerage.entity.Asset;
import com.brokerage.entity.Customer;

import java.math.BigDecimal;
import java.util.List;

public interface AssetServiceInterface {
    
    Asset findOrCreateAsset(Customer customer, String assetName);
    
    Asset getAssetWithLock(Customer customer, String assetName);
    
    void depositToAsset(Customer customer, String assetName, BigDecimal amount);
    
    void withdrawFromAsset(Asset asset, BigDecimal amount);
    
    void blockAsset(Asset asset, BigDecimal amount);
    
    void unblockAsset(Asset asset, BigDecimal amount);
    
    List<AssetResponse> getCustomerAssets(Long customerId);
    
    List<AssetResponse> getCustomerAssets(Customer customer);
    
    List<AssetResponse> getAllAssetsForCustomer(String username);
}