package com.resiliencelab.inventory.service.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class KafkaLagMetrics {

    private static final String TOPIC = "order.created";
    private static final String GROUP = "inventory-service-group";

    private final MeterRegistry meterRegistry;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private volatile double lag = 0;

    private AdminClient adminClient;

    @PostConstruct
    public void init() {

        Properties properties = new Properties();

        properties.put(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        adminClient = AdminClient.create(properties);

        Gauge.builder(
                        "resiliencelab_kafka_consumer_lag",
                        this,
                        KafkaLagMetrics::getLag
                )
                .description("Kafka consumer lag for Inventory Service")
                .tag("service", "inventory-service")
                .tag("group", GROUP)
                .tag("topic", TOPIC)
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 5000)
    public void updateLag() {

        try {

            ListConsumerGroupOffsetsResult offsetsResult =
                    adminClient.listConsumerGroupOffsets(GROUP);

            Map<TopicPartition, OffsetAndMetadata> offsets =
                    offsetsResult
                            .partitionsToOffsetAndMetadata()
                            .get(5, TimeUnit.SECONDS);

            Map<TopicPartition, Long> endOffsets =
                    adminClient
                            .listOffsets(
                                    createOffsetRequests(offsets)
                            )
                            .all()
                            .get(5, TimeUnit.SECONDS)
                            .entrySet()
                            .stream()
                            .collect(
                                    HashMap::new,
                                    (map, entry) ->
                                            map.put(
                                                    entry.getKey(),
                                                    entry.getValue().offset()
                                            ),
                                    HashMap::putAll
                            );

            double totalLag = 0;

            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry :
                    offsets.entrySet()) {

                TopicPartition partition = entry.getKey();

                if (!partition.topic().equals(TOPIC)) {
                    continue;
                }

                long currentOffset = entry.getValue().offset();

                long endOffset =
                        endOffsets.getOrDefault(partition, currentOffset);

                totalLag += Math.max(0, endOffset - currentOffset);
            }

            lag = totalLag;

        } catch (Exception e) {

            System.err.println(
                    "Failed to update Kafka lag: " + e.getMessage()
            );
        }
    }

    private Map<TopicPartition, org.apache.kafka.clients.admin.OffsetSpec>
    createOffsetRequests(
            Map<TopicPartition, OffsetAndMetadata> offsets) {

        Map<TopicPartition, org.apache.kafka.clients.admin.OffsetSpec>
                requests = new HashMap<>();

        for (TopicPartition partition : offsets.keySet()) {

            if (partition.topic().equals(TOPIC)) {

                requests.put(
                        partition,
                        org.apache.kafka.clients.admin.OffsetSpec.latest()
                );
            }
        }

        return requests;
    }

    public double getLag() {
        return lag;
    }
}