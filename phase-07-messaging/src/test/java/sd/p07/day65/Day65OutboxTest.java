package sd.p07.day65;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class Day65OutboxTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    private static final String TOPIC = "day65-order-events";
    private static Connection connection;

    @BeforeAll
    static void setUp() throws SQLException {
        connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        try (Statement statement = connection.createStatement()) {
            statement.execute(OutboxRelay.SCHEMA);
        }
        KafkaSupport.createTopic(KAFKA.getBootstrapServers(), TOPIC, 3);
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
    }

    private long countRows(String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT count(*) FROM " + table)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private KafkaProducer<String, String> producer() {
        return KafkaSupport.producer(KAFKA.getBootstrapServers(), Map.of("acks", "all"));
    }

    private List<String> drain(String group, int expected) {
        List<String> keys = new ArrayList<>();
        try (KafkaConsumer<String, String> consumer = KafkaSupport.consumer(
                KAFKA.getBootstrapServers(), group,
                Map.of(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false))) {
            consumer.subscribe(List.of(TOPIC));
            long deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos();
            while (keys.size() < expected && System.nanoTime() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    keys.add(record.key());
                }
            }
        }
        return keys;
    }

    @Test
    @DisplayName("the order row and the outbox row are written atomically")
    void bothRowsOrNeither() throws SQLException {
        new OrderService(connection).placeOrder("ord-1", "cust-1", 5_000, "{}");

        assertThat(countRows("orders")).isEqualTo(1);
        assertThat(countRows("outbox"))
                .as("one local transaction, so the event exists if and only if the order does")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("a failed order writes neither row")
    void failureRollsBackBoth() throws SQLException {
        OrderService service = new OrderService(connection);
        service.placeOrder("ord-1", "cust-1", 5_000, "{}");

        assertThatThrownBy(() -> service.placeOrder("ord-2", "cust-1", -1, "{}"))
                .isInstanceOf(SQLException.class);

        assertThat(countRows("orders")).as("the good order survives").isEqualTo(1);
        assertThat(countRows("outbox")).as("and the failed one left nothing behind").isEqualTo(1);
    }

    @Test
    @DisplayName("the before-picture: no outbox means downstream never hears about the order")
    void withoutOutboxEventsAreLost() throws SQLException {
        new OrderService(connection).placeOrderWithoutOutbox("ord-99", "cust-9", 1_000);

        assertThat(countRows("orders")).isEqualTo(1);
        assertThat(countRows("outbox"))
                .as("""
                        The order exists. No email, no shipment, no analytics - and no error
                        anywhere, because the order itself succeeded. This is the silent failure
                        the outbox pattern exists to make impossible.""")
                .isZero();
    }

    @Test
    @DisplayName("the relay publishes unpublished rows and marks them done")
    void relayPublishes() throws SQLException {
        OrderService service = new OrderService(connection);
        service.placeOrder("ord-1", "cust-1", 1_000, "{}");
        service.placeOrder("ord-2", "cust-1", 2_000, "{}");
        service.placeOrder("ord-3", "cust-2", 3_000, "{}");

        try (KafkaProducer<String, String> producer = producer()) {
            OutboxRelay relay = new OutboxRelay(connection, producer, TOPIC);

            assertThat(relay.fetchUnpublished(10)).hasSize(3);
            assertThat(relay.relayOnce(10)).isEqualTo(3);
            assertThat(relay.fetchUnpublished(10)).as("all marked published").isEmpty();
        }

        assertThat(drain("day65-consumer", 3))
                .containsExactlyInAnyOrder("ord-1", "ord-2", "ord-3");
    }

    @Test
    @DisplayName("the relay is safe to run repeatedly - a second pass publishes nothing")
    void relayIsSafeToRunRepeatedly() throws SQLException {
        new OrderService(connection).placeOrder("ord-1", "cust-1", 1_000, "{}");

        try (KafkaProducer<String, String> producer = producer()) {
            OutboxRelay relay = new OutboxRelay(connection, producer, TOPIC);

            assertThat(relay.relayOnce(10)).isEqualTo(1);
            assertThat(relay.relayOnce(10)).isZero();
            assertThat(relay.relayOnce(10)).isZero();
        }
    }

    @Test
    @DisplayName("unpublished rows come back oldest first - the id sequence is the event order")
    void relayPreservesOrder() throws SQLException {
        OrderService service = new OrderService(connection);
        for (int i = 1; i <= 5; i++) {
            service.placeOrder("ord-" + i, "cust-1", i * 1_000L, "{}");
        }

        try (KafkaProducer<String, String> producer = producer()) {
            List<OutboxRecord> pending =
                    new OutboxRelay(connection, producer, TOPIC).fetchUnpublished(10);

            assertThat(pending).extracting(OutboxRecord::aggregateId)
                    .as("publishing out of order would hand consumers a lie")
                    .containsExactly("ord-1", "ord-2", "ord-3", "ord-4", "ord-5");
        }
    }

    @Test
    @DisplayName("the batch size is respected, so a backlog drains in chunks")
    void batchSize() throws SQLException {
        OrderService service = new OrderService(connection);
        for (int i = 1; i <= 10; i++) {
            service.placeOrder("ord-" + i, "cust-1", 1_000, "{}");
        }

        try (KafkaProducer<String, String> producer = producer()) {
            OutboxRelay relay = new OutboxRelay(connection, producer, TOPIC);

            assertThat(relay.relayOnce(4)).isEqualTo(4);
            assertThat(relay.relayOnce(4)).isEqualTo(4);
            assertThat(relay.relayOnce(4)).isEqualTo(2);
            assertThat(relay.relayOnce(4)).isZero();
        }
    }
}
