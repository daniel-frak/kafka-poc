package dev.codesoapbox.kafkapoc.ordering.adapters.driving.kafka;

import dev.codesoapbox.kafkapoc.ordering.application.domain.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
public class OrderKafkaProducer {

    private static final String TOPIC = "orders";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String customerId, OrderCreatedEvent orderCreatedEvent) {
        kafkaTemplate.send(TOPIC, customerId, orderCreatedEvent);
    }
}
