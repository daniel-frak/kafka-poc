package dev.codesoapbox.kafkapoc.testing.kafka.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.extension.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * JUnit 5 extension to support Kafka Streams topology testing.
 *
 * <p>This extension initializes a {@link TopologyTestDriver} before each test
 * and provides access to it through parameter injection. It also cleans up the
 * test driver after each test has been executed.</p>
 * <p>Use a static method annotated with {@link TopologyConfiguration} to provide the topology under test.</p>
 *
 * <pre class="code">
 * &#064;KafkaTopologyTest
 * public class MyKafkaStreamsTest {
 *
 *     &#064;TopologyConfiguration
 *     public void configureTopology(StreamsBuilder builder) {
 *         // Configure your topology here
 *     }
 *
 *     &#064;BeforeEach
 *     void setUp(TopologyTestDriver testDriver) {
 *         // Your setup code here
 *     }
 *
 *     &#064;Test
 *     void testSomething(TopologyTestDriver testDriver) {
 *         // Your test code here
 *     }
 * }
 * </pre>
 */
public class KafkaTopologyTestExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private TopologyTestDriver testDriver;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Properties props = getProperties();
        StreamsBuilder kStreamBuilder = new StreamsBuilder();

        configureTopology(context, kStreamBuilder);
        testDriver = new TopologyTestDriver(kStreamBuilder.build(), props);
    }

    @SuppressWarnings("resource") // Simple Serdes don't need to be closed
    private Properties getProperties() {
        var props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "dev.codesoapbox.kafkapoc.*");

        return props;
    }

    private void configureTopology(ExtensionContext context, StreamsBuilder kStreamBuilder)
            throws IllegalAccessException, InvocationTargetException {
        Class<?> testClass = context.getRequiredTestClass();

        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(TopologyConfiguration.class)) {
                method.setAccessible(true);
                method.invoke(null, kStreamBuilder);
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == TopologyTestDriver.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return testDriver;
    }
}
