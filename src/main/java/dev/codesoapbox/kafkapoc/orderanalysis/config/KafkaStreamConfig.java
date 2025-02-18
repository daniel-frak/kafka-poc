package dev.codesoapbox.kafkapoc.orderanalysis.config;

import dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka.HourlyProductSalesInfoKTable;
import dev.codesoapbox.kafkapoc.orderanalysis.application.ProductSalesInfo;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Windowed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

@EnableKafkaStreams
@Configuration
public class KafkaStreamConfig {

    @Bean
    HourlyProductSalesInfoKTable productSalesInfoKTable(StreamsBuilderFactoryBean kafkaStreamsFactory) {
        return new HourlyProductSalesInfoKTable(kafkaStreamsFactory);
    }

    @Bean
    KTable<Windowed<Long>, ProductSalesInfo> enrichedOrderKStream(StreamsBuilder kStreamBuilder) {
        return HourlyProductSalesInfoKTable.build(kStreamBuilder);
    }
}
