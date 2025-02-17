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
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.Optional;

@RequiredArgsConstructor
public class ProductSalesInfoKTable {

    protected static final String PRODUCT_SALE_INFO_STORE_NAME = "product-sales-info";
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;

    public static KTable<Long, ProductSalesInfo> build(StreamsBuilder kStreamBuilder) {
        KStream<String, EnrichedOrder> enrichedOrderStream = kStreamBuilder.stream(EnrichedOrderListener.TOPIC);
        KGroupedStream<Long, Product> productsByProductId = getProductsByProductId(enrichedOrderStream);
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

    private static KTable<Long, ProductSalesInfo> aggregateProductSales(
            KGroupedStream<Long, Product> productsByProductId) {
        return productsByProductId
                .aggregate(ProductSalesInfo::new,
                        (key, value, aggregate) -> aggregate.process(key, value),
                        Materialized.<Long, ProductSalesInfo, KeyValueStore<Bytes, byte[]>>
                                        as(PRODUCT_SALE_INFO_STORE_NAME)
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(new JsonSerde<>(ProductSalesInfo.class)));
    }

    public Optional<ProductSalesInfo> getProductSalesInfo(long productId) {
        KafkaStreams kafkaStreams = kafkaStreamsFactory.getKafkaStreams();
        if (kafkaStreams == null) {
            return Optional.empty();
        }
        ProductSalesInfo storeValue = kafkaStreams
                .store(StoreQueryParameters.fromNameAndType(PRODUCT_SALE_INFO_STORE_NAME,
                        QueryableStoreTypes.<Long, ProductSalesInfo>keyValueStore()))
                .get(productId);

        return Optional.ofNullable(storeValue);
    }
}
