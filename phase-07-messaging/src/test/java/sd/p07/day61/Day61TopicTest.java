package sd.p07.day61;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import sd.p07.support.KafkaSupport;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day61TopicTest {

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    private String bootstrap() {
        return KAFKA.getBootstrapServers();
    }

    private static Map<String, String> messages() {
        Map<String, String> messages = new LinkedHashMap<>();
        messages.put("user-1", "logged-in");
        messages.put("user-2", "logged-in");
        messages.put("user-3", "logged-in");
        messages.put("user-4", "logged-in");
        messages.put("user-5", "logged-in");
        messages.put("user-6", "logged-in");
        return messages;
    }

    @Test
    @DisplayName("a produced record lands at a definite partition and offset")
    void produceReportsPlacement() {
        String topic = "day61-placement";
        KafkaSupport.createTopic(bootstrap(), topic, 3);

        try (KafkaProducer<String, String> producer =
                     KafkaSupport.producer(bootstrap(), Map.of())) {
            List<TopicExplorer.Placement> placements =
                    TopicExplorer.publish(producer, topic, messages());

            placements.forEach(p -> System.out.printf(
                    "  %-8s -> partition %d, offset %d%n", p.key(), p.partition(), p.offset()));

            assertThat(placements).hasSize(6);
            assertThat(placements).allSatisfy(p -> {
                assertThat(p.partition()).isBetween(0, 2);
                assertThat(p.offset()).isGreaterThanOrEqualTo(0);
            });
        }
    }

    @Test
    @DisplayName("the key decides the partition, so equal keys always land together")
    void keyDeterminesPartition() {
        String topic = "day61-keys";
        KafkaSupport.createTopic(bootstrap(), topic, 3);

        try (KafkaProducer<String, String> producer =
                     KafkaSupport.producer(bootstrap(), Map.of())) {

            Map<String, String> repeated = new LinkedHashMap<>();
            repeated.put("order-42", "created");
            List<TopicExplorer.Placement> first =
                    TopicExplorer.publish(producer, topic, repeated);

            repeated.put("order-42", "paid");
            List<TopicExplorer.Placement> second =
                    TopicExplorer.publish(producer, topic, repeated);

            assertThat(second.get(0).partition())
                    .as("this is the mechanism behind every ordering guarantee in this phase")
                    .isEqualTo(first.get(0).partition());
            assertThat(second.get(0).offset())
                    .as("and the log only ever appends")
                    .isGreaterThan(first.get(0).offset());
        }
    }

    @Test
    @DisplayName("keys spread across the partitions")
    void keysSpread() {
        String topic = "day61-spread";
        KafkaSupport.createTopic(bootstrap(), topic, 3);

        try (KafkaProducer<String, String> producer =
                     KafkaSupport.producer(bootstrap(), Map.of())) {
            Map<String, String> many = new LinkedHashMap<>();
            for (int i = 0; i < 60; i++) {
                many.put("key-" + i, "v");
            }

            Set<Integer> partitions = TopicExplorer.publish(producer, topic, many).stream()
                    .map(TopicExplorer.Placement::partition)
                    .collect(Collectors.toSet());

            assertThat(partitions).as("60 distinct keys should reach all three partitions")
                    .containsExactlyInAnyOrder(0, 1, 2);
        }
    }

    @Test
    @DisplayName("a consumer drains everything that was written")
    void consumeEverything() {
        String topic = "day61-drain";
        KafkaSupport.createTopic(bootstrap(), topic, 3);

        try (KafkaProducer<String, String> producer =
                     KafkaSupport.producer(bootstrap(), Map.of())) {
            TopicExplorer.publish(producer, topic, messages());
        }

        try (KafkaConsumer<String, String> consumer =
                     KafkaSupport.consumer(bootstrap(), "day61-group-a", Map.of())) {
            List<ConsumerRecord<String, String>> records =
                    TopicExplorer.drain(consumer, topic, 6, Duration.ofSeconds(30));

            assertThat(records).hasSize(6);
            assertThat(records).extracting(ConsumerRecord::key)
                    .containsExactlyInAnyOrder("user-1", "user-2", "user-3",
                            "user-4", "user-5", "user-6");
        }
    }

    @Test
    @DisplayName("reading does not consume: a second group sees every message again")
    void logIsNotAQueue() {
        String topic = "day61-replay";
        KafkaSupport.createTopic(bootstrap(), topic, 3);

        try (KafkaProducer<String, String> producer =
                     KafkaSupport.producer(bootstrap(), Map.of())) {
            TopicExplorer.publish(producer, topic, messages());
        }

        try (KafkaConsumer<String, String> first =
                     KafkaSupport.consumer(bootstrap(), "group-analytics", Map.of())) {
            assertThat(TopicExplorer.drain(first, topic, 6, Duration.ofSeconds(30))).hasSize(6);
        }

        try (KafkaConsumer<String, String> second =
                     KafkaSupport.consumer(bootstrap(), "group-billing", Map.of())) {
            assertThat(TopicExplorer.drain(second, topic, 6, Duration.ofSeconds(30)))
                    .as("""
                            A queue would have handed each message to exactly one reader. A log
                            keeps them, so independent consumer groups never interfere - which is
                            why one topic can feed analytics, billing and search at once.""")
                    .hasSize(6);
        }
    }
}
