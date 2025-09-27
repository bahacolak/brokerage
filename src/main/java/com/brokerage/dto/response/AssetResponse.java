package com.brokerage.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetResponse {
    
    private Long id;
    private Long customerId;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;
}