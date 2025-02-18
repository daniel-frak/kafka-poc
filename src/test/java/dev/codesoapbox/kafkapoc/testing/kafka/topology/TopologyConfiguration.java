package dev.codesoapbox.kafkapoc.testing.kafka.topology;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to mark methods that configure the Kafka Streams topology for testing
 * when using {@link KafkaTopologyTestExtension}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TopologyConfiguration {
}
