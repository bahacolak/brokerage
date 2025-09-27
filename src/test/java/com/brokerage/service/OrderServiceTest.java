package com.brokerage.service;

import com.brokerage.dto.request.CreateOrderRequest;
import com.brokerage.dto.response.OrderResponse;
import com.brokerage.entity.Asset;
import com.brokerage.entity.Customer;
import com.brokerage.entity.Order;
import com.brokerage.enums.OrderSide;
import com.brokerage.enums.OrderStatus;
import com.brokerage.exception.InsufficientBalanceException;
import com.brokerage.mapper.OrderMapper;
import com.brokerage.repository.OrderRepository;
import com.brokerage.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private AssetServiceInterface assetService;
    
    @Mock
    private CustomerServiceInterface customerService;
    
    @Mock
    private OrderMapper orderMapper;
    
    @InjectMocks
    private OrderServiceImpl orderService;
    
    private Customer testCustomer;
    private Asset tryAsset;
    private Asset stockAsset;
    private CreateOrderRequest buyRequest;
    private CreateOrderRequest sellRequest;
    private Order testOrder;
    
    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .username("test.user")
                .email("test@example.com")
                .fullName("Test User")
                .build();
        
        tryAsset = Asset.builder()
                .id(1L)
                .customer(testCustomer)
                .assetName("TRY")
                .size(new BigDecimal("10000"))
                .usableSize(new BigDecimal("10000"))
                .build();
        
        stockAsset = Asset.builder()
                .id(2L)
                .customer(testCustomer)
                .assetName("AAPL")
                .size(new BigDecimal("100"))
                .usableSize(new BigDecimal("100"))
                .build();
        
        buyRequest = CreateOrderRequest.builder()
                .assetName("AAPL")
                .side(OrderSide.BUY)
                .size(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .build();
        
        sellRequest = CreateOrderRequest.builder()
                .assetName("AAPL")
                .side(OrderSide.SELL)
                .size(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .build();
        
        testOrder = Order.builder()
                .id(1L)
                .customer(testCustomer)
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .status(OrderStatus.PENDING)
                .build();
    }
    
    @Test
    void createBuyOrder_Success() {
        when(customerService.getCustomerByUsername("test.user")).thenReturn(testCustomer);
        when(assetService.getAssetWithLock(testCustomer, "TRY")).thenReturn(tryAsset);
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(OrderResponse.builder()
                .id(1L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .status(OrderStatus.PENDING)
                .build());
        
        OrderResponse response = orderService.createOrder(buyRequest, "test.user");
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("AAPL", response.getAssetName());
        assertEquals(OrderSide.BUY, response.getOrderSide());
        
        verify(assetService).blockAsset(tryAsset, new BigDecimal("1500"));
        verify(orderRepository).save(any(Order.class));
    }
    
    @Test
    void createBuyOrder_InsufficientBalance() {
        tryAsset.setUsableSize(new BigDecimal("1000"));
        
        when(customerService.getCustomerByUsername("test.user")).thenReturn(testCustomer);
        when(assetService.getAssetWithLock(testCustomer, "TRY")).thenReturn(tryAsset);
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(testOrder);
        
        assertThrows(InsufficientBalanceException.class, () -> {
            orderService.createOrder(buyRequest, "test.user");
        });
        
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void createSellOrder_Success() {
        when(customerService.getCustomerByUsername("test.user")).thenReturn(testCustomer);
        when(assetService.getAssetWithLock(testCustomer, "AAPL")).thenReturn(stockAsset);
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(testOrder);
        testOrder.setOrderSide(OrderSide.SELL);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(OrderResponse.builder()
                .id(1L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(OrderSide.SELL)
                .size(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .status(OrderStatus.PENDING)
                .build());
        
        OrderResponse response = orderService.createOrder(sellRequest, "test.user");
        
        assertNotNull(response);
        assertEquals(OrderSide.SELL, response.getOrderSide());
        
        verify(assetService).blockAsset(stockAsset, new BigDecimal("10"));
        verify(orderRepository).save(any(Order.class));
    }
    
    @Test
    void cancelOrder_Success() {
        when(customerService.getCustomerByUsername("test.user")).thenReturn(testCustomer);
        when(customerService.isAdmin("test.user")).thenReturn(false);
        when(orderRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.of(testOrder));
        when(assetService.getAssetWithLock(testCustomer, "TRY")).thenReturn(tryAsset);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        
        orderService.cancelOrder(1L, "test.user");
        
        assertEquals(OrderStatus.CANCELED, testOrder.getStatus());
        verify(assetService).unblockAsset(tryAsset, new BigDecimal("1500"));
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    void matchOrder_BuyOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(assetService.getAssetWithLock(testCustomer, "TRY")).thenReturn(tryAsset);
        when(assetService.findOrCreateAsset(testCustomer, "AAPL")).thenReturn(stockAsset);
        
        orderService.matchOrder(1L);
        
        assertEquals(OrderStatus.MATCHED, testOrder.getStatus());
        verify(assetService).withdrawFromAsset(tryAsset, new BigDecimal("1500"));
        verify(orderRepository).save(testOrder);
    }
}