package dev.codesoapbox.kafkapoc.ordering.adapters.driving.http;

import dev.codesoapbox.kafkapoc.ordering.adapters.driving.kafka.OrderKafkaProducer;
import dev.codesoapbox.kafkapoc.ordering.application.domain.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderKafkaProducer orderKafkaProducer;

    @PostMapping
    public void order(String customerId, @RequestBody OrderCreatedEvent orderCreatedEvent) {
        orderKafkaProducer.publish(customerId, orderCreatedEvent);
    }
}
