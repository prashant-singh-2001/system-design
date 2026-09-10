package sd.p05.day46;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day46SqlVsNoSqlTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private final UserProfileService service = new UserProfileService();

    private Connection rawConnection() throws Exception {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @BeforeEach
    void resetSchemaAndSeedUser() throws Exception {
        try (Connection conn = rawConnection(); Statement statement = conn.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS orders, user_profile_view, users");
            statement.execute("""
                    CREATE TABLE users (id BIGSERIAL PRIMARY KEY, name TEXT NOT NULL, email TEXT NOT NULL)""");
            statement.execute("""
                    CREATE TABLE orders (
                        id BIGINT PRIMARY KEY,
                        user_id BIGINT NOT NULL REFERENCES users(id),
                        total_cents BIGINT NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now()
                    )""");
            statement.execute("""
                    CREATE TABLE user_profile_view (
                        user_id BIGINT PRIMARY KEY REFERENCES users(id),
                        name TEXT NOT NULL,
                        email TEXT NOT NULL,
                        recent_order_ids BIGINT[] NOT NULL DEFAULT '{}',
                        recent_order_totals_cents BIGINT[] NOT NULL DEFAULT '{}'
                    )""");
            statement.execute(
                    "INSERT INTO users (id, name, email) VALUES (1, 'Alice', 'alice@example.com')");
            statement.execute(
                    "INSERT INTO user_profile_view (user_id, name, email) "
                            + "VALUES (1, 'Alice', 'alice@example.com')");
        }
    }

    @Test
    @DisplayName("both models agree on the user's profile and their 5 most recent orders")
    void bothModelsAgree() throws Exception {
        try (Connection conn = rawConnection()) {
            for (long orderId = 1; orderId <= 7; orderId++) {
                service.recordOrder(conn, 1, orderId, orderId * 1_000);
            }

            UserProfile viaJoin = service.fetchViaJoin(conn, 1);
            UserProfile viaView = service.fetchViaDenormalizedView(conn, 1);

            List<OrderSummary> expectedRecent = List.of(
                    new OrderSummary(7, 7_000),
                    new OrderSummary(6, 6_000),
                    new OrderSummary(5, 5_000),
                    new OrderSummary(4, 4_000),
                    new OrderSummary(3, 3_000));

            assertThat(viaJoin.name()).isEqualTo("Alice");
            assertThat(viaJoin.email()).isEqualTo("alice@example.com");
            assertThat(viaJoin.recentOrders()).containsExactlyElementsOf(expectedRecent);

            assertThat(viaView)
                    .as("the denormalized read must agree with the joined read, byte for byte")
                    .isEqualTo(viaJoin);
        }
    }

    @Test
    @DisplayName("recording an order is more expensive under denormalization: two writes, not one")
    void recordOrderCostsTwoWrites() throws Exception {
        try (Connection conn = rawConnection()) {
            AtomicInteger writeCount = new AtomicInteger(0);
            Connection counted = CountingConnection.wrap(conn, writeCount);

            service.recordOrder(counted, 1, 1, 5_000);

            assertThat(writeCount)
                    .as("one INSERT into orders, one UPDATE keeping user_profile_view in sync")
                    .hasValue(2);
        }
    }

    @Test
    @DisplayName("a query against the denormalized view takes exactly one round trip")
    void denormalizedReadIsOneQuery() throws Exception {
        try (Connection conn = rawConnection()) {
            service.recordOrder(conn, 1, 1, 5_000);

            AtomicInteger readCount = new AtomicInteger(0);
            Connection counted = CountingConnection.wrap(conn, readCount);

            service.fetchViaDenormalizedView(counted, 1);

            assertThat(readCount).hasValue(1);
        }
    }
}
