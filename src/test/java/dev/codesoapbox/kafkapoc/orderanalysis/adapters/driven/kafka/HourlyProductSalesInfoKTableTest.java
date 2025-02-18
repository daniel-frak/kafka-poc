package dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka;

import dev.codesoapbox.kafkapoc.orderanalysis.application.ProductSalesInfo;
import dev.codesoapbox.kafkapoc.ordering.application.domain.Product;
import dev.codesoapbox.kafkapoc.testing.kafka.topology.KafkaTopologyTest;
import dev.codesoapbox.kafkapoc.testing.kafka.topology.TopologyConfiguration;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@KafkaTopologyTest
class HourlyProductSalesInfoKTableTest {

    private TestInputTopic<String, EnrichedOrder> enrichedOrderTopic;
    private WindowStore<Long, ProductSalesInfo> store;

    @TopologyConfiguration
    static void configureTopology(StreamsBuilder kStreamBuilder) {
        HourlyProductSalesInfoKTable.build(kStreamBuilder);
    }

    @SuppressWarnings("resource") // Simple Serdes don't need to be closed
    @BeforeEach
    void setUp(TopologyTestDriver testDriver) {
        enrichedOrderTopic = testDriver.createInputTopic(
                EnrichedOrderListener.TOPIC,
                Serdes.String().serializer(),
                new JsonSerializer<>());
        store = testDriver.getWindowStore(HourlyProductSalesInfoKTable.STORE_NAME);
    }

    @Test
    void shouldProcessCorrectlyGivenOrdersFromMultipleCustomers() {
        var product1 = new Product(1L, "Product 1", BigDecimal.valueOf(100.0));
        var product2 = new Product(2L, "Product 2", BigDecimal.valueOf(300.0));

        enrichedOrderTopic.pipeInput("customer-1", new EnrichedOrder("order-1", List.of(product1, product2)),
                1000L);
        enrichedOrderTopic.pipeInput("customer-2", new EnrichedOrder("order-2", List.of(product1)),
                2000L);

        long timeFrom = 0;
        long timeTo = 0;

        assertThatStoreContains(product1.id(), timeFrom, timeTo, new ProductSalesInfo(
                product1.id(),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(200.00),
                2
        ));
        assertThatStoreContains(product2.id(), timeFrom, timeTo, new ProductSalesInfo(
                product2.id(),
                BigDecimal.valueOf(300.00),
                BigDecimal.valueOf(300.00),
                1
        ));
    }

    private void assertThatStoreContains(long id, long timeFrom, long timeTo, ProductSalesInfo expected) {
        ProductSalesInfo result = getFromStore(id, timeFrom, timeTo);
        assertThat(result).isEqualTo(expected);
    }

    private ProductSalesInfo getFromStore(long productId, long timeFrom, long timeTo) {
        try (WindowStoreIterator<ProductSalesInfo> iterator = store.fetch(productId, timeFrom, timeTo)) {
            return iterator.next().value;
        }
    }

    @Test
    void shouldUseHourlyTumblingWindow() {
        var product1 = new Product(1L, "Product 1", BigDecimal.valueOf(100.0));

        enrichedOrderTopic.pipeInput("customer-1", new EnrichedOrder("order-1", List.of(product1)),
                Duration.ofHours(1).toMillis() - 1);
        enrichedOrderTopic.pipeInput("customer-1", new EnrichedOrder("order-2", List.of(product1)),
                Duration.ofHours(1).toMillis());

        long timeFrom = 0;
        long timeTo = 0;

        assertThatStoreContains(product1.id(), timeFrom, timeTo, new ProductSalesInfo(
                product1.id(),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(100.00),
                1
        ));
    }
}