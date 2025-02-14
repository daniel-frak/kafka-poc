package dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka;


import dev.codesoapbox.kafkapoc.orderanalysis.adapters.driving.kafka.EnrichedOrderKafkaProducer;
import dev.codesoapbox.kafkapoc.orderanalysis.application.ProductRepository;
import dev.codesoapbox.kafkapoc.ordering.application.domain.OrderCreatedEvent;
import dev.codesoapbox.kafkapoc.ordering.application.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class OrderListener {

    private static final String TOPIC = "orders";

    private final ProductRepository productRepository;
    private final EnrichedOrderKafkaProducer enrichedOrderKafkaProducer;

    @KafkaListener(id = "order-listener-1", topics = TOPIC)
    public void listen(@Payload OrderCreatedEvent orderCreatedEvent,
                       @Header(KafkaHeaders.RECEIVED_KEY) String customerId,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) {
        log.info("Payload received for key={} partition={} topic={}, timestamp={}:\n{}",
                customerId, partition, topic, timestamp, orderCreatedEvent);

        List<Product> products = getProducts(orderCreatedEvent);
        var enrichedOrder = new EnrichedOrder(orderCreatedEvent.orderId(), products);
        enrichedOrderKafkaProducer.publish(customerId, enrichedOrder);
    }

    private List<Product> getProducts(OrderCreatedEvent orderCreatedEvent) {
        return orderCreatedEvent.productIds().stream()
                .map(productRepository::getById)
                .toList();
    }
}
