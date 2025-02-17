package dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka;

import dev.codesoapbox.kafkapoc.orderanalysis.application.ProductSalesInfo;
import dev.codesoapbox.kafkapoc.ordering.application.domain.Product;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSalesInfoKTableTest {

    private TopologyTestDriver testDriver;

    private TestInputTopic<String, EnrichedOrder> enrichedOrderTopic;

    @BeforeEach
    void setUp() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "dev.codesoapbox.kafkapoc.*");
        StreamsBuilder kStreamBuilder = new StreamsBuilder();
        ProductSalesInfoKTable.build(kStreamBuilder);
        testDriver = new TopologyTestDriver(kStreamBuilder.build(), props);

        enrichedOrderTopic = testDriver.createInputTopic(
                EnrichedOrderListener.TOPIC,
                Serdes.String().serializer(),
                new JsonSerializer<>());
    }

    @AfterEach
    public void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldProcessCorrectlyGivenOrdersFromMultipleCustomers() {
        var order = new EnrichedOrder("order-1", List.of(
                new Product(1L, "Product1", BigDecimal.valueOf(100.0)),
                new Product(2L, "Product2", BigDecimal.valueOf(200.0))
        ));

        enrichedOrderTopic.pipeInput("customer-1", order);
        enrichedOrderTopic.pipeInput("customer-2", order);

        ReadOnlyKeyValueStore<Long, ProductSalesInfo> keyValueStore =
                testDriver.getKeyValueStore("product-sales-info");

        ProductSalesInfo result1 = keyValueStore.get(1L);
        assertThat(result1).isEqualTo(new ProductSalesInfo(
                1L,
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(200.00),
                2
        ));
        ProductSalesInfo result2 = keyValueStore.get(2L);
        assertThat(result2).isEqualTo(new ProductSalesInfo(
                2L,
                BigDecimal.valueOf(200.00),
                BigDecimal.valueOf(400.00),
                2
        ));
    }
}