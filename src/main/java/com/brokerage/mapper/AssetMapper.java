package com.brokerage.mapper;

import com.brokerage.dto.response.AssetResponse;
import com.brokerage.entity.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    
    @Mapping(source = "customer.id", target = "customerId")
    AssetResponse toResponse(Asset asset);
    
    List<AssetResponse> toResponseList(List<Asset> assets);
}