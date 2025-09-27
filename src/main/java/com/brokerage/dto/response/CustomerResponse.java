package com.brokerage.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private boolean active;
    private LocalDateTime createdAt;
}