package com.rbkmoney.wb.list.manager;

import com.rbkmoney.kafka.common.serialization.ThriftSerializer;
import com.rbkmoney.wb.list.manager.extension.RiakContainerExtension;
import com.rbkmoney.wb.list.manager.serializer.EventDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Testcontainers
@Slf4j
@ExtendWith({SpringExtension.class, RiakContainerExtension.class})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(initializers = KafkaAbstractTest.Initializer.class)
public abstract class KafkaAbstractTest {

    private static final String CONFLUENT_IMAGE_NAME = "confluentinc/cp-kafka";
    private static final String CONFLUENT_PLATFORM_VERSION = "6.0.3";

    @Container
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName
            .parse(CONFLUENT_IMAGE_NAME)
            .withTag(CONFLUENT_PLATFORM_VERSION))
            .withEmbeddedZookeeper()
            .withStartupTimeout(Duration.ofMinutes(2));

    @DynamicPropertySource
    static void connectionConfigs(DynamicPropertyRegistry registry) {
        registry.add("riak.port", () -> RiakContainerExtension.RIAK.getMappedPort(8087));
    }

    public static <T> Consumer<String, T> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(props);
    }

    public static <T> Producer<String, T> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "CLIENT");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ThriftSerializer.class);
        return new KafkaProducer<>(props);
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues
                    .of("kafka.bootstrap.servers=" + kafka.getBootstrapServers())
                    .applyTo(configurableApplicationContext.getEnvironment());
            initTopic("wb-list-command");
            initTopic("wb-list-event-sink");
        }

        private <T> Consumer<String, T> initTopic(String topicName) {
            Consumer<String, T> consumer = createConsumer();
            try {
                consumer.subscribe(Collections.singletonList(topicName));
                consumer.poll(Duration.ofSeconds(1));
            } catch (Exception e) {
                log.error("KafkaAbstractTest initialize e: ", e);
            }
            consumer.close();
            return consumer;
        }
    }

}
