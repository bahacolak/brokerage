package com.brokerage.service.impl;

import com.brokerage.dto.response.AssetResponse;
import com.brokerage.entity.Asset;
import com.brokerage.entity.Customer;
import com.brokerage.exception.ResourceNotFoundException;
import com.brokerage.mapper.AssetMapper;
import com.brokerage.repository.AssetRepository;
import com.brokerage.service.AssetServiceInterface;
import com.brokerage.service.CustomerServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssetServiceImpl implements AssetServiceInterface {
    
    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;
    
    public Asset findOrCreateAsset(Customer customer, String assetName) {
        return assetRepository.findByCustomerAndAssetName(customer, assetName)
                .orElseGet(() -> {
                    Asset newAsset = Asset.builder()
                            .customer(customer)
                            .assetName(assetName)
                            .size(BigDecimal.ZERO)
                            .usableSize(BigDecimal.ZERO)
                            .build();
                    return assetRepository.save(newAsset);
                });
    }
    
    public Asset getAssetWithLock(Customer customer, String assetName) {
        return assetRepository.findByCustomerAndAssetNameWithLock(customer, assetName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Asset %s not found for customer %s", assetName, customer.getUsername())));
    }
    
    public void depositToAsset(Customer customer, String assetName, BigDecimal amount) {
        Asset asset = findOrCreateAsset(customer, assetName);
        asset.setSize(asset.getSize().add(amount));
        asset.setUsableSize(asset.getUsableSize().add(amount));
        assetRepository.save(asset);
        log.info("Deposited {} {} to customer {}", amount, assetName, customer.getUsername());
    }
    
    public void withdrawFromAsset(Asset asset, BigDecimal amount) {
        asset.setSize(asset.getSize().subtract(amount));
        asset.setUsableSize(asset.getUsableSize().subtract(amount));
        assetRepository.save(asset);
    }
    
    public void blockAsset(Asset asset, BigDecimal amount) {
        asset.setUsableSize(asset.getUsableSize().subtract(amount));
        assetRepository.save(asset);
    }
    
    public void unblockAsset(Asset asset, BigDecimal amount) {
        asset.setUsableSize(asset.getUsableSize().add(amount));
        assetRepository.save(asset);
    }
    
    @Transactional(readOnly = true)
    public List<AssetResponse> getCustomerAssets(Long customerId) {
        List<Asset> assets = assetRepository.findByCustomerId(customerId);
        return assetMapper.toResponseList(assets);
    }
    
    @Transactional(readOnly = true)
    public List<AssetResponse> getCustomerAssets(Customer customer) {
        List<Asset> assets = assetRepository.findByCustomer(customer);
        return assetMapper.toResponseList(assets);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> getAllAssetsForCustomer(String username) {
        throw new UnsupportedOperationException("Use getCustomerAssets with Customer object instead");
    }
}