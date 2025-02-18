package dev.codesoapbox.kafkapoc.testing.kafka.topology;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to enable Kafka topology testing using {@link KafkaTopologyTestExtension}.
 **/
@ExtendWith(KafkaTopologyTestExtension.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaTopologyTest {
}
