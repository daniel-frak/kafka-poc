package dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@Slf4j
public class EnrichedOrderListener {

    private static final String TOPIC = "enriched-orders";

    @KafkaListener(id = "enriched-order-listener-1", topics = TOPIC)
    public void listen(@Payload EnrichedOrder enrichedOrder,
                       @Header(KafkaHeaders.RECEIVED_KEY) String customerId,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) {
        log.info("Payload received for key={} partition={} topic={}, timestamp={}:\n{}",
                customerId, partition, topic, timestamp, enrichedOrder);
    }
}
