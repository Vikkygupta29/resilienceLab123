package com.resiliencelab.order.service.config;

import com.resiliencelab.order.service.dto.event.InventoryFailedEvent;
import com.resiliencelab.order.service.dto.event.InventoryReservedEvent;
import com.resiliencelab.order.service.dto.event.PaymentCompletedEvent;
import com.resiliencelab.order.service.dto.event.PaymentFailedEvent;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final MeterRegistry meterRegistry;

    public KafkaConsumerConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }


    // ==========================================
    // Inventory Reserved Consumer
    // ==========================================

    @Bean
    public ConsumerFactory<String, InventoryReservedEvent>
    inventoryConsumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-service-inventory-group"
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
                "com.resiliencelab.order.service.dto.event"
        );

        props.put(
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.resiliencelab.order.service.dto.event.InventoryReservedEvent"
        );

        DefaultKafkaConsumerFactory<String, InventoryReservedEvent> factory =
                new DefaultKafkaConsumerFactory<>(props);

        factory.addListener(
                new MicrometerConsumerListener<>(meterRegistry)
        );

        return factory;
    }


    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservedEvent>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, InventoryReservedEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, InventoryReservedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }


    // ==========================================
    // Payment Completed Consumer
    // ==========================================

    @Bean
    public ConsumerFactory<String, PaymentCompletedEvent>
    paymentConsumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-service-payment-group"
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
                "com.resiliencelab.order.service.dto.event"
        );

        props.put(
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.resiliencelab.order.service.dto.event.PaymentCompletedEvent"
        );

        DefaultKafkaConsumerFactory<String, PaymentCompletedEvent> factory =
                new DefaultKafkaConsumerFactory<>(props);

        factory.addListener(
                new MicrometerConsumerListener<>(meterRegistry)
        );

        return factory;
    }


    @Bean(name = "paymentKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent>
    paymentKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentCompletedEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }


    // ==========================================
    // Inventory Failed Consumer
    // ==========================================

    @Bean
    public ConsumerFactory<String, InventoryFailedEvent>
    inventoryFailedConsumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-service-inventory-failed-group"
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
                "com.resiliencelab.order.service.dto.event"
        );

        props.put(
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.resiliencelab.order.service.dto.event.InventoryFailedEvent"
        );

        DefaultKafkaConsumerFactory<String, InventoryFailedEvent> factory =
                new DefaultKafkaConsumerFactory<>(props);

        factory.addListener(
                new MicrometerConsumerListener<>(meterRegistry)
        );

        return factory;
    }


    @Bean(name = "inventoryFailedKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, InventoryFailedEvent>
    inventoryFailedKafkaListenerContainerFactory(
            ConsumerFactory<String, InventoryFailedEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, InventoryFailedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }


    // ==========================================
    // Payment Failed Consumer
    // ==========================================

    @Bean
    public ConsumerFactory<String, PaymentFailedEvent>
    paymentFailedConsumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-service-payment-failed-group"
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
                "com.resiliencelab.order.service.dto.event"
        );

        props.put(
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.resiliencelab.order.service.dto.event.PaymentFailedEvent"
        );

        DefaultKafkaConsumerFactory<String, PaymentFailedEvent> factory =
                new DefaultKafkaConsumerFactory<>(props);

        factory.addListener(
                new MicrometerConsumerListener<>(meterRegistry)
        );

        return factory;
    }


    @Bean(name = "paymentFailedKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent>
    paymentFailedKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentFailedEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}