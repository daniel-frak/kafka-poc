package dev.codesoapbox.kafkapoc.ordering.config;

import dev.codesoapbox.kafkapoc.orderanalysis.adapters.driving.kafka.EnrichedOrderKafkaProducer;
import dev.codesoapbox.kafkapoc.ordering.adapters.driving.kafka.OrderKafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class OrderingKafkaBeanConfig {

    @Bean
    public OrderKafkaProducer orderProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        return new OrderKafkaProducer(kafkaTemplate);
    }

    @Bean
    public EnrichedOrderKafkaProducer enrichedOrderProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        return new EnrichedOrderKafkaProducer(kafkaTemplate);
    }
}
