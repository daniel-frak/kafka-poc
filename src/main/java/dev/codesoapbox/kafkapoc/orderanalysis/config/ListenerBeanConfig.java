package dev.codesoapbox.kafkapoc.orderanalysis.config;

import dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka.EnrichedOrderListener;
import dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka.OrderListener;
import dev.codesoapbox.kafkapoc.orderanalysis.adapters.driving.kafka.EnrichedOrderKafkaProducer;
import dev.codesoapbox.kafkapoc.orderanalysis.application.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListenerBeanConfig {

    @Bean
    OrderListener orderListener(EnrichedOrderKafkaProducer enrichedOrderKafkaProducer) {
        return new OrderListener(new ProductRepository(), enrichedOrderKafkaProducer);
    }

    @Bean
    EnrichedOrderListener enrichedOrderListener() {
        return new EnrichedOrderListener();
    }
}
