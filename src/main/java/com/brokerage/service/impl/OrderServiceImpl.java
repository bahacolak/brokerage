package com.brokerage.service.impl;

import com.brokerage.dto.request.CreateOrderRequest;
import com.brokerage.dto.request.CreateOrderForCustomerRequest;
import com.brokerage.dto.request.ListOrdersRequest;
import com.brokerage.dto.response.OrderResponse;
import com.brokerage.entity.Asset;
import com.brokerage.entity.Customer;
import com.brokerage.entity.Order;
import com.brokerage.enums.OrderSide;
import com.brokerage.enums.OrderStatus;
import com.brokerage.exception.BrokerageException;
import com.brokerage.exception.InsufficientBalanceException;
import com.brokerage.exception.ResourceNotFoundException;
import com.brokerage.mapper.OrderMapper;
import com.brokerage.repository.OrderRepository;
import com.brokerage.service.AssetServiceInterface;
import com.brokerage.service.CustomerServiceInterface;
import com.brokerage.service.OrderServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderServiceInterface {
    
    private static final String TRY_ASSET = "TRY";
    
    private final OrderRepository orderRepository;
    private final AssetServiceInterface assetService;
    private final CustomerServiceInterface customerService;
    private final OrderMapper orderMapper;
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrderResponse createOrder(CreateOrderRequest request, String username) {
        Customer customer = customerService.getCustomerByUsername(username);
        
        validateOrderRequest(request);
        
        Order order = orderMapper.toEntity(request);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        
        if (request.getSide() == OrderSide.BUY) {
            processBuyOrder(customer, request);
        } else {
            processSellOrder(customer, request);
        }
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created: {} for customer {}", savedOrder.getId(), customer.getUsername());
        
        return orderMapper.toResponse(savedOrder);
    }
    
    private void processBuyOrder(Customer customer, CreateOrderRequest request) {
        BigDecimal totalCost = request.getPrice().multiply(request.getSize());
        Asset tryAsset = assetService.getAssetWithLock(customer, TRY_ASSET);
        
        if (tryAsset.getUsableSize().compareTo(totalCost) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient TRY balance. Required: %s, Available: %s", 
                            totalCost, tryAsset.getUsableSize()));
        }
        
        assetService.blockAsset(tryAsset, totalCost);
    }
    
    private void processSellOrder(Customer customer, CreateOrderRequest request) {
        Asset asset = assetService.getAssetWithLock(customer, request.getAssetName());
        
        if (asset.getUsableSize().compareTo(request.getSize()) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient %s balance. Required: %s, Available: %s", 
                            request.getAssetName(), request.getSize(), asset.getUsableSize()));
        }
        
        assetService.blockAsset(asset, request.getSize());
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void cancelOrder(Long orderId, String username) {
        Customer customer = customerService.getCustomerByUsername(username);
        Order order;
        
        if (customerService.isAdmin(username)) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        } else {
            order = orderRepository.findByIdAndCustomerId(orderId, customer.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found or you don't have permission to cancel it"));
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BrokerageException(
                    String.format("Cannot cancel order with status %s", order.getStatus()),
                    HttpStatus.BAD_REQUEST);
        }
        
        rollbackOrderAssets(order);
        
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
        
        log.info("Order {} canceled by user {}", orderId, username);
    }
    
    private void rollbackOrderAssets(Order order) {
        if (order.getOrderSide() == OrderSide.BUY) {
            BigDecimal totalCost = order.getPrice().multiply(order.getSize());
            Asset tryAsset = assetService.getAssetWithLock(order.getCustomer(), TRY_ASSET);
            assetService.unblockAsset(tryAsset, totalCost);
        } else {
            Asset asset = assetService.getAssetWithLock(order.getCustomer(), order.getAssetName());
            assetService.unblockAsset(asset, order.getSize());
        }
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void matchOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BrokerageException(
                    String.format("Cannot match order with status %s", order.getStatus()),
                    HttpStatus.BAD_REQUEST);
        }
        
        executeOrderMatching(order);
        
        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);
        
        log.info("Order {} matched", orderId);
    }
    
    private void executeOrderMatching(Order order) {
        if (order.getOrderSide() == OrderSide.BUY) {
            executeBuyOrderMatching(order);
        } else {
            executeSellOrderMatching(order);
        }
    }
    
    private void executeBuyOrderMatching(Order order) {
        BigDecimal totalCost = order.getPrice().multiply(order.getSize());
        
        Asset tryAsset = assetService.getAssetWithLock(order.getCustomer(), TRY_ASSET);
        assetService.withdrawFromAsset(tryAsset, totalCost);
        
        Asset targetAsset = assetService.findOrCreateAsset(order.getCustomer(), order.getAssetName());
        targetAsset.setSize(targetAsset.getSize().add(order.getSize()));
        targetAsset.setUsableSize(targetAsset.getUsableSize().add(order.getSize()));
    }
    
    private void executeSellOrderMatching(Order order) {
        Asset asset = assetService.getAssetWithLock(order.getCustomer(), order.getAssetName());
        assetService.withdrawFromAsset(asset, order.getSize());
        
        BigDecimal totalRevenue = order.getPrice().multiply(order.getSize());
        Asset tryAsset = assetService.findOrCreateAsset(order.getCustomer(), TRY_ASSET);
        tryAsset.setSize(tryAsset.getSize().add(totalRevenue));
        tryAsset.setUsableSize(tryAsset.getUsableSize().add(totalRevenue));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(ListOrdersRequest request) {
        List<Order> orders;
        
        if (request.getCustomerId() != null && request.getStartDate() != null && request.getEndDate() != null) {
            orders = orderRepository.findByCustomerIdAndCreateDateBetween(
                    request.getCustomerId(), request.getStartDate(), request.getEndDate());
        } else if (request.getCustomerId() != null) {
            orders = orderRepository.findByCustomerId(request.getCustomerId());
        } else {
            orders = orderRepository.findAll();
        }
        
        if (request.getStatus() != null) {
            orders = orders.stream()
                    .filter(order -> order.getStatus() == request.getStatus())
                    .collect(Collectors.toList());
        }
        
        if (request.getAssetName() != null) {
            orders = orders.stream()
                    .filter(order -> order.getAssetName().equals(request.getAssetName()))
                    .collect(Collectors.toList());
        }
        
        return orderMapper.toResponseList(orders);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders;
        if (startDate != null && endDate != null) {
            orders = orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
        } else {
            orders = orderRepository.findByCustomerId(customerId);
        }
        return orderMapper.toResponseList(orders);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForCurrentUser(String username, LocalDateTime startDate, LocalDateTime endDate) {
        Customer customer = customerService.getCustomerByUsername(username);
        return getCustomerOrders(customer.getId(), startDate, endDate);
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrderResponse createOrderForCustomer(CreateOrderForCustomerRequest request) {
        Customer customer = customerService.getCustomerById(request.getCustomerId());
        
        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .assetName(request.getAssetName())
                .side(request.getSide())
                .size(request.getSize())
                .price(request.getPrice())
                .build();
        
        validateOrderRequest(orderRequest);
        
        Order order = orderMapper.toEntity(orderRequest);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        
        if (request.getSide() == OrderSide.BUY) {
            processBuyOrder(customer, orderRequest);
        } else {
            processSellOrder(customer, orderRequest);
        }
        
        Order savedOrder = orderRepository.save(order);
        log.info("Admin created order: {} for customer {}", savedOrder.getId(), customer.getUsername());
        
        return orderMapper.toResponse(savedOrder);
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void cancelOrderAsAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BrokerageException(
                    String.format("Cannot cancel order with status %s", order.getStatus()),
                    HttpStatus.BAD_REQUEST);
        }
        
        rollbackOrderAssets(order);
        
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
        
        log.info("Admin canceled order {}", orderId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> listAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orderMapper.toResponseList(orders);
    }
    
    private void validateOrderRequest(CreateOrderRequest request) {
        if (request.getSide() == OrderSide.SELL && TRY_ASSET.equals(request.getAssetName())) {
            throw new BrokerageException("Cannot sell TRY directly", HttpStatus.BAD_REQUEST);
        }
        
        if (request.getSide() == OrderSide.BUY && TRY_ASSET.equals(request.getAssetName())) {
            throw new BrokerageException("Cannot buy TRY directly", HttpStatus.BAD_REQUEST);
        }
    }
}