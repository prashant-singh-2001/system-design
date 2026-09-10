package sd.p07.day64;

import org.apache.kafka.clients.consumer.ConsumerConfig;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day64OrderingTest {

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final List<String> LIFECYCLE =
            List.of("created", "validated", "paid", "packed", "shipped", "delivered");

    private String bootstrap() {
        return KAFKA.getBootstrapServers();
    }

    private KafkaConsumer<String, String> consumer(String group) {
        return KafkaSupport.consumer(bootstrap(), group,
                Map.of(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false));
    }

    @Test
    @DisplayName("one key means one partition, so the sequence is preserved exactly")
    void keyedEventsStayOrdered() {
        String topic = "day64-keyed";
        KafkaSupport.createTopic(bootstrap(), topic, 6);

        try (KafkaProducer<String, String> producer =
                     KafkaSupport.producer(bootstrap(), Map.of())) {
            int partition = OrderingLab.publishKeyed(producer, topic, "order-42", LIFECYCLE);
            System.out.printf("  all six events for order-42 landed on partition %d%n", partition);
        }

        try (KafkaConsumer<String, String> consumer = consumer("day64-keyed-group")) {
            Map<Integer, List<String>> byPartition =
                    OrderingLab.drainByPartition(consumer, topic, LIFECYCLE.size(), TIMEOUT);

            assertThat(byPartition)
                    .as("a single key must not be spread across partitions")
                    .hasSize(1);
            assertThat(byPartition.values().iterator().next())
                    .as("""
                            This is the guarantee you actually rely on: an order is never seen
                            shipped before it was created.""")
                    .containsExactlyElementsOf(LIFECYCLE);
        }
    }

    @Test
    @DisplayName("different keys go to different partitions and proceed in parallel")
    void differentKeysAreIndependent() {
        String topic = "day64-multikey";
        KafkaSupport.createTopic(bootstrap(), topic, 6);

        try (KafkaProducer<String, String> producer =
                     KafkaSupport.producer(bootstrap(), Map.of())) {
            OrderingLab.publishKeyed(producer, topic, "order-1", List.of("a1", "a2", "a3"));
            OrderingLab.publishKeyed(producer, topic, "order-2", List.of("b1", "b2", "b3"));
            OrderingLab.publishKeyed(producer, topic, "order-3", List.of("c1", "c2", "c3"));
        }

        try (KafkaConsumer<String, String> consumer = consumer("day64-multikey-group")) {
            Map<Integer, List<String>> byPartition =
                    OrderingLab.drainByPartition(consumer, topic, 9, TIMEOUT);

            // Each order's own events must appear in order, wherever they landed.
            for (String prefix : List.of("a", "b", "c")) {
                List<String> forOrder = byPartition.values().stream()
                        .flatMap(List::stream)
                        .filter(v -> v.startsWith(prefix))
                        .toList();
                assertThat(forOrder)
                        .as("events for one order must stay in sequence")
                        .containsExactly(prefix + "1", prefix + "2", prefix + "3");
            }
        }
    }

    @Test
    @DisplayName("no key means no ordering unit - records scatter across partitions")
    void unkeyedEventsScatter() {
        String topic = "day64-unkeyed";
        KafkaSupport.createTopic(bootstrap(), topic, 6);

        try (KafkaProducer<String, String> producer =
                     KafkaSupport.producer(bootstrap(), Map.of())) {
            List<String> values = new java.util.ArrayList<>();
            for (int i = 0; i < 300; i++) {
                values.add("event-" + i);
            }

            Set<Integer> partitions =
                    Set.copyOf(OrderingLab.publishUnkeyed(producer, topic, values));

            System.out.println("  unkeyed records used partitions " + partitions);

            assertThat(partitions)
                    .as("""
                            With no key there is no ordering unit. A consumer may legitimately see
                            'shipped' before 'created', and there is no bug to find - you never
                            asked for ordering. Choosing the partition key is a correctness
                            decision, not a performance one.""")
                    .hasSizeGreaterThan(1);
        }
    }

    @Test
    @DisplayName("global ordering is possible - and costs you all your parallelism")
    void globalOrderingMeansOnePartition() {
        String topic = "day64-global";
        KafkaSupport.createTopic(bootstrap(), topic, 1);

        try (KafkaProducer<String, String> producer =
                     KafkaSupport.producer(bootstrap(), Map.of())) {
            List<String> values = new java.util.ArrayList<>();
            for (int i = 0; i < 50; i++) {
                values.add("event-" + i);
            }
            OrderingLab.publishUnkeyed(producer, topic, values);
        }

        try (KafkaConsumer<String, String> consumer = consumer("day64-global-group")) {
            Map<Integer, List<String>> byPartition =
                    OrderingLab.drainByPartition(consumer, topic, 50, TIMEOUT);

            assertThat(byPartition).hasSize(1);
            assertThat(byPartition.get(0)).hasSize(50);
            assertThat(byPartition.get(0).get(0)).isEqualTo("event-0");
            assertThat(byPartition.get(0).get(49))
                    .as("""
                            Total ordering, guaranteed - and now exactly one consumer can work on
                            this topic, so your throughput ceiling is a single machine. This is the
                            same trade as Day 49's sharding, because it is the same mechanism.""")
                    .isEqualTo("event-49");
        }
    }
}
