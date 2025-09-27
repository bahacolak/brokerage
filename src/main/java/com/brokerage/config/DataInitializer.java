package com.brokerage.config;

import com.brokerage.entity.Customer;
import com.brokerage.repository.CustomerRepository;
import com.brokerage.service.AssetServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AssetServiceInterface assetService;
    
    @Bean
    @Profile("!test")
    CommandLineRunner initDatabase() {
        return args -> {
            if (!customerRepository.existsByUsername("admin")) {
                Customer admin = Customer.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@brokerage.com")
                        .fullName("System Administrator")
                        .roles(Set.of("ROLE_ADMIN"))
                        .active(true)
                        .build();
                
                customerRepository.save(admin);
                log.info("Admin user created with username: admin and password: admin123");
            }
            
            if (!customerRepository.existsByUsername("testuser")) {
                Customer testUser = Customer.builder()
                        .username("testuser")
                        .password(passwordEncoder.encode("test123"))
                        .email("test@example.com")
                        .fullName("Test User")
                        .roles(Set.of("ROLE_CUSTOMER"))
                        .active(true)
                        .build();
                
                Customer savedUser = customerRepository.save(testUser);
                assetService.depositToAsset(savedUser, "TRY", new BigDecimal("10000"));
                log.info("Test user created with username: testuser and password: test123 with 10000 TRY");
            }
        };
    }
}