package com.resiliencelab.payment.service.repository;

import com.resiliencelab.payment.service.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ProcessedEventRepository
        extends JpaRepository<ProcessedEvent, UUID> {

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO processed_events
            (event_id, consumer_name, processed_at)
            VALUES (:eventId, :consumerName, :processedAt)
            """, nativeQuery = true)
    int tryMarkAsProcessed(
            @Param("eventId") UUID eventId,
            @Param("consumerName") String consumerName,
            @Param("processedAt") LocalDateTime processedAt
    );
}