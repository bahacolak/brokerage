package com.brokerage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "assets", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "asset_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(name = "asset_name", nullable = false)
    private String assetName;
    
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal size = BigDecimal.ZERO;
    
    @Column(name = "usable_size", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal usableSize = BigDecimal.ZERO;
    
    @Version
    private Long version;
}