package com.resiliencelab.order.service.service;

import com.resiliencelab.order.service.dto.OrderRequest;
import com.resiliencelab.order.service.dto.OrderResponse;
import com.resiliencelab.order.service.enums.OrderStatus;

import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrder(UUID orderId);

    long countByStatus(OrderStatus status);

}
