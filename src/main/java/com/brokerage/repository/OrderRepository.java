package com.brokerage.repository;

import com.brokerage.entity.Customer;
import com.brokerage.entity.Order;
import com.brokerage.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByCustomer(Customer customer);
    
    List<Order> findByCustomerId(Long customerId);
    
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId " +
           "AND o.createDate BETWEEN :startDate AND :endDate")
    List<Order> findByCustomerIdAndCreateDateBetween(@Param("customerId") Long customerId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.customer = :customer " +
           "AND o.createDate BETWEEN :startDate AND :endDate")
    List<Order> findByCustomerAndCreateDateBetween(@Param("customer") Customer customer,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);
    
    Optional<Order> findByIdAndCustomer(Long id, Customer customer);
    
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findPendingOrders(@Param("status") OrderStatus status);
}