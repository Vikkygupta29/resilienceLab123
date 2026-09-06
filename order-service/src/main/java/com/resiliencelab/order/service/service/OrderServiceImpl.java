package com.resiliencelab.order.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.resiliencelab.order.service.dto.event.OrderCreatedEvent;
import com.resiliencelab.order.service.dto.OrderRequest;
import com.resiliencelab.order.service.dto.OrderResponse;
import com.resiliencelab.order.service.entity.Order;
import com.resiliencelab.order.service.entity.OutboxEvent;
import com.resiliencelab.order.service.exception.OrderNotFoundException;
import com.resiliencelab.order.service.messaging.OrderEventProducer;
import com.resiliencelab.order.service.repository.OrderRepository;
import com.resiliencelab.order.service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        Order order = Order.create(
                request.productId(),
                request.quantity(),
                request.amount()
        );

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                savedOrder.getId(),
                savedOrder.getProductId(),
                savedOrder.getAmount(),
                savedOrder.getQuantity()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent(
                    event.eventId(),
                    "OrderCreatedEvent",
                    "order.created",
                    payload
            );

            outboxEventRepository.save(outboxEvent);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize order event", e);
        }

        return OrderResponse.from(savedOrder);
    }

    @Override
    public OrderResponse getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new OrderNotFoundException("order not found")
        );

        return OrderResponse.from(order);
    }

}
