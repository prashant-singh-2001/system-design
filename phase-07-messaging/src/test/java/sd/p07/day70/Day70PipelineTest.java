package sd.p07.day70;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import sd.p07.support.KafkaSupport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day70PipelineTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static Connection connection;
    private String topic;

    @BeforeAll
    static void setUp() throws SQLException {
        connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        try (Statement statement = connection.createStatement()) {
            statement.execute(OrderPipeline.SCHEMA);
        }
    }

    @AfterAll
    static void tearDown() throws SQLException {
        connection.close();
    }

    @BeforeEach
    void reset() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE orders, outbox RESTART IDENTITY");
        }
        topic = "day70-" + UUID.randomUUID().toString().substring(0, 8);
        KafkaSupport.createTopic(KAFKA.getBootstrapServers(), topic, 3);
    }

    private KafkaProducer<String, String> producer() {
        return KafkaSupport.producer(KAFKA.getBootstrapServers(), Map.of(
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true));
    }

    private KafkaConsumer<String, String> consumer(String group) {
        return KafkaSupport.consumer(KAFKA.getBootstrapServers(), group,
                Map.of(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false));
    }

    @Test
    @DisplayName("end to end: orders become a revenue projection")
    void endToEnd() throws SQLException {
        try (KafkaProducer<String, String> producer = producer()) {
            OrderPipeline pipeline = new OrderPipeline(connection, producer, topic);

            pipeline.placeOrder("ord-1", "alice", 5_000);
            pipeline.placeOrder("ord-2", "bob", 3_000);
            pipeline.placeOrder("ord-3", "alice", 2_500);

            assertThat(pipeline.relay(10)).isEqualTo(3);

            try (KafkaConsumer<String, String> consumer = consumer("day70-projection")) {
                OrderPipeline.ConsumeResult result = pipeline.consumeIntoProjection(
                        consumer, 3, TIMEOUT, new HashSet<>());

                System.out.println("  revenue by customer: " + result.revenueByCustomer());

                assertThat(result.consumed()).isEqualTo(3);
                assertThat(result.revenueByCustomer())
                        .containsEntry("alice", 7_500L)
                        .containsEntry("bob", 3_000L);
            }
        }
    }

    @Test
    @DisplayName("nothing is published for an order that failed to commit")
    void atomicity() throws SQLException {
        try (KafkaProducer<String, String> producer = producer()) {
            OrderPipeline pipeline = new OrderPipeline(connection, producer, topic);

            pipeline.placeOrder("ord-1", "alice", 5_000);
            try {
                pipeline.placeOrder("ord-2", "bob", -1);
            } catch (SQLException expected) {
                // the CHECK constraint refused it and the transaction rolled back
            }

            assertThat(pipeline.relay(10))
                    .as("only the successful order produced an event")
                    .isEqualTo(1);
        }
    }

    @Test
    @DisplayName("THE conclusion: at-least-once delivery is safe because processing is idempotent")
    void duplicatesAreSuppressed() throws SQLException {
        try (KafkaProducer<String, String> producer = producer()) {
            OrderPipeline pipeline = new OrderPipeline(connection, producer, topic);

            pipeline.placeOrder("ord-1", "alice", 5_000);
            pipeline.relay(10);

            Set<String> processed = new HashSet<>();

            try (KafkaConsumer<String, String> first = consumer("day70-group-a")) {
                OrderPipeline.ConsumeResult result =
                        pipeline.consumeIntoProjection(first, 1, TIMEOUT, processed);
                assertThat(result.revenueByCustomer()).containsEntry("alice", 5_000L);
            }

            // The same events arrive again: a rebalance, a replay, a redeployed consumer.
            try (KafkaConsumer<String, String> second = consumer("day70-group-b")) {
                OrderPipeline.ConsumeResult replay =
                        pipeline.consumeIntoProjection(second, 1, TIMEOUT, processed);

                assertThat(replay.duplicatesSuppressed())
                        .as("the projection recognised an event it had already applied")
                        .isEqualTo(1);
                assertThat(replay.revenueByCustomer())
                        .as("""
                                Without idempotency alice's revenue would now read 10,000 - a
                                silent doubling caused by a redelivery nobody did wrong. This is
                                the whole reason at-least-once is safe to build on.""")
                        .doesNotContainEntry("alice", 10_000L);
            }
        }
    }

    @Test
    @DisplayName("a customer's events are keyed together, so their order is preserved")
    void perCustomerOrdering() throws SQLException {
        try (KafkaProducer<String, String> producer = producer()) {
            OrderPipeline pipeline = new OrderPipeline(connection, producer, topic);

            for (int i = 1; i <= 6; i++) {
                pipeline.placeOrder("ord-" + i, "alice", i * 1_000L);
            }
            pipeline.relay(10);

            try (KafkaConsumer<String, String> consumer = consumer("day70-ordering")) {
                OrderPipeline.ConsumeResult result = pipeline.consumeIntoProjection(
                        consumer, 6, TIMEOUT, new HashSet<>());

                assertThat(result.revenueByCustomer())
                        .as("1000+2000+3000+4000+5000+6000")
                        .containsEntry("alice", 21_000L);
            }
        }
    }

    @Test
    @DisplayName("the relay drains a backlog and then has nothing left to do")
    void relayDrainsAndStops() throws SQLException {
        try (KafkaProducer<String, String> producer = producer()) {
            OrderPipeline pipeline = new OrderPipeline(connection, producer, topic);

            for (int i = 1; i <= 7; i++) {
                pipeline.placeOrder("ord-" + i, "cust-" + i, 1_000);
            }

            assertThat(pipeline.relay(3)).isEqualTo(3);
            assertThat(pipeline.relay(3)).isEqualTo(3);
            assertThat(pipeline.relay(3)).isEqualTo(1);
            assertThat(pipeline.relay(3)).isZero();
        }
    }
}
