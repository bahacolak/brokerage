package com.brokerage.dto.request;

import com.brokerage.enums.OrderSide;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    
    @NotBlank(message = "Asset name is required")
    private String assetName;
    
    @NotNull(message = "Order side is required")
    private OrderSide side;
    
    @NotNull(message = "Size is required")
    @DecimalMin(value = "0.0001", message = "Size must be greater than 0")
    private BigDecimal size;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0001", message = "Price must be greater than 0")
    private BigDecimal price;
}