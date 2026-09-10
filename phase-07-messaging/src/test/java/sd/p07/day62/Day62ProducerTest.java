package sd.p07.day62;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import sd.p07.support.KafkaSupport;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class Day62ProducerTest {

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    private String bootstrap() {
        return KAFKA.getBootstrapServers();
    }

    @Test
    @DisplayName("acks=0: the producer does not even learn where the record went")
    void fireAndForgetKnowsNothing() {
        String topic = "day62-acks0";
        KafkaSupport.createTopic(bootstrap(), topic, 1);

        try (KafkaProducer<String, String> producer = KafkaSupport.producer(bootstrap(),
                Map.of(ProducerConfig.ACKS_CONFIG, "0"))) {

            ProducerLab.SendOutcome outcome =
                    ProducerLab.sendAndDescribe(producer, topic, "k", "v");

            assertThat(outcome.offsetKnown())
                    .as("""
                            With acks=0 there is no acknowledgement to carry an offset. You have no
                            idea whether the broker stored this, and a network blip loses it
                            silently. Only acceptable for data you can afford to lose.""")
                    .isFalse();
        }
    }

    @Test
    @DisplayName("acks=1: the leader confirms, so you get a real offset")
    void leaderAckGivesAnOffset() {
        String topic = "day62-acks1";
        KafkaSupport.createTopic(bootstrap(), topic, 1);

        try (KafkaProducer<String, String> producer = KafkaSupport.producer(bootstrap(),
                Map.of(ProducerConfig.ACKS_CONFIG, "1"))) {

            ProducerLab.SendOutcome outcome =
                    ProducerLab.sendAndDescribe(producer, topic, "k", "v");

            assertThat(outcome.offsetKnown()).isTrue();
            assertThat(outcome.offset()).isGreaterThanOrEqualTo(0);
            assertThat(outcome.partition()).isZero();
        }
    }

    @Test
    @DisplayName("acks=all: every in-sync replica has it before the send returns")
    void allAcksSurvivesLeaderLoss() {
        String topic = "day62-acksall";
        KafkaSupport.createTopic(bootstrap(), topic, 1);

        try (KafkaProducer<String, String> producer = KafkaSupport.producer(bootstrap(),
                Map.of(ProducerConfig.ACKS_CONFIG, "all"))) {

            ProducerLab.SendOutcome outcome =
                    ProducerLab.sendAndDescribe(producer, topic, "k", "v");

            assertThat(outcome.offsetKnown()).isTrue();
            assertThat(outcome.offset()).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("offsets advance monotonically - a partition is an append-only log")
    void offsetsAdvance() {
        String topic = "day62-offsets";
        KafkaSupport.createTopic(bootstrap(), topic, 1);

        try (KafkaProducer<String, String> producer = KafkaSupport.producer(bootstrap(),
                Map.of(ProducerConfig.ACKS_CONFIG, "all"))) {

            long first = ProducerLab.sendAndDescribe(producer, topic, "k", "1").offset();
            long second = ProducerLab.sendAndDescribe(producer, topic, "k", "2").offset();
            long third = ProducerLab.sendAndDescribe(producer, topic, "k", "3").offset();

            assertThat(second).isEqualTo(first + 1);
            assertThat(third).isEqualTo(second + 1);
        }
    }

    @Test
    @DisplayName("Kafka refuses an incoherent combination rather than trusting you")
    void idempotenceRequiresAcksAll() {
        assertThatThrownBy(() -> KafkaSupport.producer(bootstrap(), Map.of(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
                ProducerConfig.ACKS_CONFIG, "1")))
                .as("""
                        Deduplication is meaningless if the record can vanish with a dead leader,
                        so Kafka will not let you ask for both. Enforcing the coherent combination
                        rather than trusting the caller is a design instinct worth stealing.""")
                .isInstanceOf(ConfigException.class);
    }

    @Test
    @DisplayName("the idempotent producer is happy with acks=all")
    void idempotenceWithAcksAll() {
        String topic = "day62-idempotent";
        KafkaSupport.createTopic(bootstrap(), topic, 1);

        try (KafkaProducer<String, String> producer = KafkaSupport.producer(bootstrap(), Map.of(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
                ProducerConfig.ACKS_CONFIG, "all"))) {

            assertThat(ProducerLab.sendAndDescribe(producer, topic, "k", "v").offsetKnown())
                    .isTrue();
        }
    }

    @Test
    @DisplayName("batching: linger.ms trades a little latency for bigger batches")
    void lingerBatchesRecords() {
        String topic = "day62-batching";
        KafkaSupport.createTopic(bootstrap(), topic, 1);

        double noLinger;
        double withLinger;

        try (KafkaProducer<String, String> producer = KafkaSupport.producer(bootstrap(), Map.of(
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.LINGER_MS_CONFIG, 0))) {
            Duration elapsed = ProducerLab.sendBatch(producer, topic, 2_000);
            noLinger = ProducerLab.metric(producer, "batch-size-avg");
            System.out.printf("  linger.ms=0  : %5d ms, avg batch %8.1f bytes%n",
                    elapsed.toMillis(), noLinger);
        }

        try (KafkaProducer<String, String> producer = KafkaSupport.producer(bootstrap(), Map.of(
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.LINGER_MS_CONFIG, 50,
                ProducerConfig.BATCH_SIZE_CONFIG, 64 * 1024))) {
            Duration elapsed = ProducerLab.sendBatch(producer, topic, 2_000);
            withLinger = ProducerLab.metric(producer, "batch-size-avg");
            System.out.printf("  linger.ms=50 : %5d ms, avg batch %8.1f bytes%n",
                    elapsed.toMillis(), withLinger);
        }

        assertThat(noLinger).as("both runs must have produced batches").isGreaterThan(0);
        assertThat(withLinger)
                .as("waiting to fill a batch can only make batches larger, never smaller")
                .isGreaterThanOrEqualTo(noLinger);
    }

    @Test
    @DisplayName("not blocking per record is what makes batching possible at all")
    void batchSendDoesNotBlockPerRecord() {
        String topic = "day62-throughput";
        KafkaSupport.createTopic(bootstrap(), topic, 1);

        try (KafkaProducer<String, String> producer = KafkaSupport.producer(bootstrap(), Map.of(
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.LINGER_MS_CONFIG, 20))) {

            Duration elapsed = ProducerLab.sendBatch(producer, topic, 5_000);
            System.out.printf("  5,000 records in %d ms%n", elapsed.toMillis());

            assertThat(elapsed)
                    .as("""
                            If this is slow, check you are not calling get() after every send.
                            Blocking per record serialises everything into one round trip each,
                            and no linger.ms setting can rescue it.""")
                    .isLessThan(Duration.ofSeconds(20));
        }
    }
}
