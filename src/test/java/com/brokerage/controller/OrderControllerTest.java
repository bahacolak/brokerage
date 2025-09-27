package com.brokerage.controller;

import com.brokerage.dto.request.CreateOrderRequest;
import com.brokerage.dto.response.OrderResponse;
import com.brokerage.entity.Customer;
import com.brokerage.enums.OrderSide;
import com.brokerage.enums.OrderStatus;
import com.brokerage.service.CustomerServiceInterface;
import com.brokerage.service.OrderServiceInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private OrderServiceInterface orderService;
    
    @MockBean
    private CustomerServiceInterface customerService;
    
    private Customer testCustomer;
    private CreateOrderRequest createOrderRequest;
    private OrderResponse orderResponse;
    
    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .username("test.user")
                .email("test@example.com")
                .fullName("Test User")
                .build();
        
        createOrderRequest = CreateOrderRequest.builder()
                .assetName("AAPL")
                .side(OrderSide.BUY)
                .size(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .build();
        
        orderResponse = OrderResponse.builder()
                .id(1L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();
    }
    
    @Test
    @WithMockUser(username = "test.user", roles = {"CUSTOMER"})
    void createOrder_Success() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class), any(String.class))).thenReturn(orderResponse);
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.assetName").value("AAPL"));
    }
    
    @Test
    @WithMockUser(username = "test.user", roles = {"CUSTOMER"})
    void createOrder_InvalidRequest() throws Exception {
        createOrderRequest.setSize(null);
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "test.user", roles = {"CUSTOMER"})
    void listOrders_Success() throws Exception {
        List<OrderResponse> orders = Arrays.asList(orderResponse);
        when(orderService.getOrdersForCurrentUser(any(String.class), any(), any())).thenReturn(orders);
        
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void listOrders_AdminCanViewAllOrders() throws Exception {
        List<OrderResponse> orders = Arrays.asList(orderResponse);
        when(orderService.getOrdersForCurrentUser(any(String.class), any(), any())).thenReturn(orders);
        
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(username = "test.user", roles = {"CUSTOMER"})
    void cancelOrder_Success() throws Exception {
        
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order canceled successfully"));
    }
    
    @Test
    void unauthenticatedRequest_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }
}