package com.resiliencelab.order.service.repository;

import com.resiliencelab.order.service.entity.Order;
import com.resiliencelab.order.service.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    long countByStatus(OrderStatus status);
}
