package com.resiliencelab.order.service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resiliencelab.order.service.dto.event.OrderCreatedEvent;
import com.resiliencelab.order.service.entity.OutboxEvent;
import com.resiliencelab.order.service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxEvent outboxEvent : events) {

            try {
                OrderCreatedEvent event =
                        objectMapper.readValue(
                                outboxEvent.getPayload(),
                                OrderCreatedEvent.class
                        );

                kafkaTemplate.send(
                        outboxEvent.getTopic(),
                        event.orderId().toString(),
                        event
                ).get();

                outboxEvent.markAsPublished();
                outboxEventRepository.save(outboxEvent);

                System.out.println(
                        "Outbox event published: "
                                + outboxEvent.getEventId()
                );

            } catch (Exception e) {

                System.out.println(
                        "Failed to publish outbox event: "
                                + outboxEvent.getEventId()
                );

                e.printStackTrace();
            }
        }
    }
}