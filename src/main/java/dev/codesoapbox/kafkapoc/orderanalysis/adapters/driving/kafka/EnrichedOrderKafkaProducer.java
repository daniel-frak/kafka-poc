package dev.codesoapbox.kafkapoc.orderanalysis.adapters.driving.kafka;

import dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka.EnrichedOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
public class EnrichedOrderKafkaProducer {

    protected static final String TOPIC = "enriched-orders";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String customerId, EnrichedOrder enrichedOrder) {
        kafkaTemplate.send(TOPIC, customerId, enrichedOrder);
    }
}
