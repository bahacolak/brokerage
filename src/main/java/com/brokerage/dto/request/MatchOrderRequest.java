package com.brokerage.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchOrderRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
}