package com.resiliencelab.inventory.service.repository;

import com.resiliencelab.inventory.service.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

    boolean existsByEventIdAndConsumerName(String eventId, String consumerName);
}
