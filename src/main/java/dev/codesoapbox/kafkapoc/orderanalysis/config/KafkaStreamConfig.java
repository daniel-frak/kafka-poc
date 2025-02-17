package dev.codesoapbox.kafkapoc.orderanalysis.config;

import dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka.ProductSalesInfoKTable;
import dev.codesoapbox.kafkapoc.orderanalysis.application.ProductSalesInfo;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

@EnableKafkaStreams
@Configuration
public class KafkaStreamConfig {

    @Bean
    ProductSalesInfoKTable productSalesInfoKTable(StreamsBuilderFactoryBean kafkaStreamsFactory) {
        return new ProductSalesInfoKTable(kafkaStreamsFactory);
    }

    @Bean
    KTable<Long, ProductSalesInfo> enrichedOrderKStream(StreamsBuilder kStreamBuilder) {
        return ProductSalesInfoKTable.build(kStreamBuilder);
    }
}
