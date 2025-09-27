package com.brokerage.repository;

import com.brokerage.entity.Asset;
import com.brokerage.entity.Customer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asset a WHERE a.customer = :customer AND a.assetName = :assetName")
    Optional<Asset> findByCustomerAndAssetNameWithLock(@Param("customer") Customer customer, 
                                                       @Param("assetName") String assetName);
    
    Optional<Asset> findByCustomerAndAssetName(Customer customer, String assetName);
    
    List<Asset> findByCustomer(Customer customer);
    
    List<Asset> findByCustomerId(Long customerId);
    
    @Query("SELECT a FROM Asset a WHERE a.customer.id = :customerId AND a.assetName = :assetName")
    Optional<Asset> findByCustomerIdAndAssetName(@Param("customerId") Long customerId, 
                                                 @Param("assetName") String assetName);
}