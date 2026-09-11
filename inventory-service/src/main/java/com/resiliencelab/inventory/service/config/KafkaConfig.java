package com.resiliencelab.inventory.service.config;

import com.resiliencelab.inventory.service.dto.event.InventoryFailedEvent;
import com.resiliencelab.inventory.service.messaging.InventoryReservedEvent;
import com.resiliencelab.inventory.service.messaging.OrderCreatedEvent;

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

import org.springframework.kafka.core.*;

import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private final MeterRegistry meterRegistry;

    public KafkaConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }


    // ==========================================
    // CONSUMER CONFIGURATION
    // ==========================================

    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> consumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JacksonJsonDeserializer.class
        );

        props.put(
                JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS,
                false
        );

        props.put(
                JacksonJsonDeserializer.TRUSTED_PACKAGES,
                "com.resiliencelab.inventory.service.messaging"
        );

        props.put(
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.resiliencelab.inventory.service.messaging.OrderCreatedEvent"
        );

        DefaultKafkaConsumerFactory<String, OrderCreatedEvent> factory =
                new DefaultKafkaConsumerFactory<>(props);

        factory.addListener(
                new MicrometerConsumerListener<>(meterRegistry)
        );

        return factory;
    }


    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, OrderCreatedEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }


    // ==========================================
    // PRODUCER: InventoryReservedEvent
    // ==========================================

    @Bean
    public ProducerFactory<String, InventoryReservedEvent>
    inventoryReservedProducerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(props);
    }


    @Bean
    public KafkaTemplate<String, InventoryReservedEvent>
    inventoryReservedKafkaTemplate(
            ProducerFactory<String, InventoryReservedEvent>
                    inventoryReservedProducerFactory) {

        return new KafkaTemplate<>(inventoryReservedProducerFactory);
    }


    // ==========================================
    // PRODUCER: InventoryFailedEvent
    // ==========================================

    @Bean
    public ProducerFactory<String, InventoryFailedEvent>
    inventoryFailedProducerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(props);
    }


    @Bean
    public KafkaTemplate<String, InventoryFailedEvent>
    inventoryFailedKafkaTemplate(
            ProducerFactory<String, InventoryFailedEvent>
                    inventoryFailedProducerFactory) {

        return new KafkaTemplate<>(inventoryFailedProducerFactory);
    }

    @Bean(name = "defaultRetryTopicKafkaTemplate")
    public KafkaTemplate<String, Object> defaultRetryTopicKafkaTemplate() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        ProducerFactory<String, Object> producerFactory =
                new DefaultKafkaProducerFactory<>(props);

        return new KafkaTemplate<>(producerFactory);
    }
}