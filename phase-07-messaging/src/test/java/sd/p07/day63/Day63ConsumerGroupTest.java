package sd.p07.day63;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import sd.p07.support.KafkaSupport;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day63ConsumerGroupTest {

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final Map<String, Object> MANUAL_COMMIT =
            Map.of(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

    private String bootstrap() {
        return KAFKA.getBootstrapServers();
    }

    private void seed(String topic, int partitions, int count) {
        KafkaSupport.createTopic(bootstrap(), topic, partitions);
        try (KafkaProducer<String, String> producer =
                     KafkaSupport.producer(bootstrap(), Map.of())) {
            for (int i = 0; i < count; i++) {
                producer.send(new ProducerRecord<>(topic, "key-" + i, "message-" + i)).get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("within a group, partitions are split - never shared")
    void groupMembersSplitPartitions() {
        String topic = "day63-split";
        seed(topic, 4, 0);

        try (KafkaConsumer<String, String> a =
                     KafkaSupport.consumer(bootstrap(), "group-split", MANUAL_COMMIT);
             KafkaConsumer<String, String> b =
                     KafkaSupport.consumer(bootstrap(), "group-split", MANUAL_COMMIT)) {

            Set<TopicPartition> first = ConsumerLab.awaitAssignment(a, topic, TIMEOUT);
            Set<TopicPartition> second = ConsumerLab.awaitAssignment(b, topic, TIMEOUT);

            System.out.println("  consumer a owns " + first);
            System.out.println("  consumer b owns " + second);

            assertThat(first).isNotEmpty();
            assertThat(second).isNotEmpty();
            assertThat(first).as("a partition belongs to exactly one member of a group")
                    .doesNotContainAnyElementsOf(second);
        }
    }

    @Test
    @DisplayName("a group cannot scale past its partition count")
    void partitionCountCapsParallelism() {
        String topic = "day63-cap";
        seed(topic, 1, 0);

        try (KafkaConsumer<String, String> a =
                     KafkaSupport.consumer(bootstrap(), "group-cap", MANUAL_COMMIT);
             KafkaConsumer<String, String> b =
                     KafkaSupport.consumer(bootstrap(), "group-cap", MANUAL_COMMIT)) {

            Set<TopicPartition> first = ConsumerLab.awaitAssignment(a, topic, TIMEOUT);
            Set<TopicPartition> second =
                    ConsumerLab.awaitAssignment(b, topic, Duration.ofSeconds(8));

            assertThat(first.size() + second.size())
                    .as("one partition means exactly one working consumer; the other idles")
                    .isEqualTo(1);
        }
    }

    @Test
    @DisplayName("different groups are independent - each sees everything")
    void groupsAreIndependent() {
        String topic = "day63-groups";
        seed(topic, 2, 6);

        try (KafkaConsumer<String, String> analytics =
                     KafkaSupport.consumer(bootstrap(), "group-analytics", MANUAL_COMMIT)) {
            assertThat(ConsumerLab.atLeastOnce(analytics, topic, 6, TIMEOUT, false).processed())
                    .hasSize(6);
        }

        try (KafkaConsumer<String, String> billing =
                     KafkaSupport.consumer(bootstrap(), "group-billing", MANUAL_COMMIT)) {
            assertThat(ConsumerLab.atLeastOnce(billing, topic, 6, TIMEOUT, false).processed())
                    .as("one topic can feed analytics, billing and search at once")
                    .hasSize(6);
        }
    }

    @Test
    @DisplayName("AT-LEAST-ONCE: a crash before the commit means reprocessing, not loss")
    void atLeastOnceDuplicates() {
        String topic = "day63-atleastonce";
        seed(topic, 1, 4);

        ConsumerLab.Delivery first;
        try (KafkaConsumer<String, String> crashing =
                     KafkaSupport.consumer(bootstrap(), "group-alo", MANUAL_COMMIT)) {
            first = ConsumerLab.atLeastOnce(crashing, topic, 4, TIMEOUT, true);
        }

        assertThat(first.processed()).as("the work was done").hasSize(4);
        assertThat(first.committed()).as("but never acknowledged").isFalse();

        try (KafkaConsumer<String, String> replacement =
                     KafkaSupport.consumer(bootstrap(), "group-alo", MANUAL_COMMIT)) {
            ConsumerLab.Delivery second =
                    ConsumerLab.atLeastOnce(replacement, topic, 4, TIMEOUT, false);

            assertThat(second.processed())
                    .as("""
                            Every record is handled a second time. Nothing was lost - but if
                            processing charges a card, you have just charged it twice. This is
                            precisely why at-least-once demands idempotent processing (Day 74).""")
                    .containsExactlyElementsOf(first.processed());
        }
    }

    @Test
    @DisplayName("AT-MOST-ONCE: a crash after the commit means silent loss")
    void atMostOnceLoses() {
        String topic = "day63-atmostonce";
        seed(topic, 1, 4);

        ConsumerLab.Delivery first;
        try (KafkaConsumer<String, String> crashing =
                     KafkaSupport.consumer(bootstrap(), "group-amo", MANUAL_COMMIT)) {
            first = ConsumerLab.atMostOnce(crashing, topic, 4, TIMEOUT, true);
        }

        assertThat(first.committed()).isTrue();
        assertThat(first.processed()).as("committed, then died before doing the work").isEmpty();

        try (KafkaConsumer<String, String> replacement =
                     KafkaSupport.consumer(bootstrap(), "group-amo", MANUAL_COMMIT)) {
            ConsumerLab.Delivery second =
                    ConsumerLab.atMostOnce(replacement, topic, 4, Duration.ofSeconds(8), false);

            assertThat(second.processed())
                    .as("""
                            The offset advanced past four records nobody handled. They are gone,
                            and nothing raised an error anywhere. Silent loss is far harder to
                            detect than duplication - which is why almost nobody chooses this.""")
                    .isEmpty();
        }
    }

    @Test
    @DisplayName("a committed at-least-once consumer does not reprocess")
    void committedWorkIsNotRepeated() {
        String topic = "day63-committed";
        seed(topic, 1, 4);

        try (KafkaConsumer<String, String> consumer =
                     KafkaSupport.consumer(bootstrap(), "group-clean", MANUAL_COMMIT)) {
            assertThat(ConsumerLab.atLeastOnce(consumer, topic, 4, TIMEOUT, false).processed())
                    .hasSize(4);
        }

        try (KafkaConsumer<String, String> next =
                     KafkaSupport.consumer(bootstrap(), "group-clean", MANUAL_COMMIT)) {
            assertThat(ConsumerLab.atLeastOnce(next, topic, 4, Duration.ofSeconds(8), false)
                    .processed())
                    .isEmpty();
        }
    }
}
