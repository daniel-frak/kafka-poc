package dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka;

import dev.codesoapbox.kafkapoc.orderanalysis.application.ProductSalesInfo;
import dev.codesoapbox.kafkapoc.ordering.application.domain.Product;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
public class HourlyProductSalesInfoKTable {

    protected static final String STORE_NAME = "product-sales-info-hourly";
    protected static final Duration WINDOW_SIZE = Duration.ofHours(1);
    protected static final Duration WINDOW_GRACE = Duration.ofMinutes(5);

    private final StreamsBuilderFactoryBean kafkaStreamsFactory;

    public static KTable<Windowed<Long>, ProductSalesInfo> build(StreamsBuilder kStreamBuilder) {
        KStream<String, EnrichedOrder> enrichedOrderStream = kStreamBuilder.stream(EnrichedOrderListener.TOPIC);
        TimeWindowedKStream<Long, Product> productsByProductId = getProductsByProductId(enrichedOrderStream)
                .windowedBy(TimeWindows.ofSizeAndGrace(WINDOW_SIZE, WINDOW_GRACE));
        return aggregateProductSales(productsByProductId);
    }

    private static KGroupedStream<Long, Product> getProductsByProductId(
            KStream<String, EnrichedOrder> enrichedOrderStream) {
        return enrichedOrderStream
                .flatMap((customerId, enrichedOrder) -> enrichedOrder.products().stream()
                        .map(product -> new KeyValue<>(product.id(), product))
                        .toList())
                .groupByKey(Grouped.with(Serdes.Long(), new JsonSerde<>(Product.class)));
    }

    private static KTable<Windowed<Long>, ProductSalesInfo> aggregateProductSales(
            TimeWindowedKStream<Long, Product> productsByProductId) {
        return productsByProductId
                .aggregate(ProductSalesInfo::new,
                        (key, value, aggregate) -> aggregate.process(key, value),
                        Materialized.<Long, ProductSalesInfo, WindowStore<Bytes, byte[]>>
                                        as(STORE_NAME)
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(new JsonSerde<>(ProductSalesInfo.class)));
    }

    public Optional<ProductSalesInfo> getProductSalesInfo(long productId) {
        KafkaStreams kafkaStreams = kafkaStreamsFactory.getKafkaStreams();
        if (kafkaStreams == null) {
            return Optional.empty();
        }
        ProductSalesInfo storeValue = kafkaStreams
                .store(StoreQueryParameters.fromNameAndType(STORE_NAME,
                        QueryableStoreTypes.<Long, ProductSalesInfo>keyValueStore()))
                .get(productId);

        return Optional.ofNullable(storeValue);
    }
}
