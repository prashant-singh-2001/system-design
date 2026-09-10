package sd.p05.day41;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Docker starts here. This test spins up a REAL, disposable Postgres via Testcontainers, applies
 * {@code src/main/resources/sd/p05/day41/schema.sql}, and then tries to break it - every
 * constraint listed in that file should refuse exactly the bad data it was written to refuse.
 */
@Testcontainers
class Day41SchemaTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static Connection connection;

    @BeforeAll
    static void applySchema() throws Exception {
        connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        String schemaSql = Files.readString(Path.of("src/main/resources/sd/p05/day41/schema.sql"));
        try (Statement statement = connection.createStatement()) {
            statement.execute(schemaSql);
        }
    }

    @AfterAll
    static void closeConnection() throws Exception {
        connection.close();
    }

    @BeforeEach
    void resetTables() throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE order_items, orders, products, customers RESTART IDENTITY CASCADE");
        }
    }

    @Test
    @DisplayName("valid customers, products, orders and order_items all insert cleanly")
    void validInsertsSucceed() throws Exception {
        long customerId = insertCustomer("a@example.com", "Alice");
        long productId = insertProduct("SKU-1", "Widget", 1_000);
        long orderId = insertOrder(customerId, "DRAFT");

        insertOrderItem(orderId, productId, 2, 1_000);
    }

    @Test
    @DisplayName("a duplicate customer email violates the UNIQUE constraint")
    void duplicateEmailIsRejected() throws Exception {
        insertCustomer("a@example.com", "Alice");

        assertThatThrownBy(() -> insertCustomer("a@example.com", "Someone Else"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("a duplicate product sku violates the UNIQUE constraint")
    void duplicateSkuIsRejected() throws Exception {
        insertProduct("SKU-1", "Widget", 1_000);

        assertThatThrownBy(() -> insertProduct("SKU-1", "A Different Widget", 2_000))
                .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("a non-positive product price violates the CHECK constraint")
    void nonPositivePriceIsRejected() {
        assertThatThrownBy(() -> insertProduct("SKU-1", "Widget", 0))
                .isInstanceOf(SQLException.class);
        assertThatThrownBy(() -> insertProduct("SKU-2", "Widget", -500))
                .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("an order referencing an unknown customer violates the foreign key")
    void unknownCustomerReferenceIsRejected() {
        assertThatThrownBy(() -> insertOrder(9_999_999L, "DRAFT"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("an order status outside the allowed set violates the CHECK constraint")
    void invalidStatusIsRejected() throws Exception {
        long customerId = insertCustomer("a@example.com", "Alice");

        assertThatThrownBy(() -> insertOrder(customerId, "SHIPPED"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("a non-positive order_items quantity violates its CHECK constraint")
    void nonPositiveQuantityIsRejected() throws Exception {
        long customerId = insertCustomer("a@example.com", "Alice");
        long productId = insertProduct("SKU-1", "Widget", 1_000);
        long orderId = insertOrder(customerId, "DRAFT");

        assertThatThrownBy(() -> insertOrderItem(orderId, productId, 0, 1_000))
                .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("the same product cannot appear twice on one order - the composite primary key")
    void duplicateOrderItemIsRejected() throws Exception {
        long customerId = insertCustomer("a@example.com", "Alice");
        long productId = insertProduct("SKU-1", "Widget", 1_000);
        long orderId = insertOrder(customerId, "DRAFT");
        insertOrderItem(orderId, productId, 1, 1_000);

        assertThatThrownBy(() -> insertOrderItem(orderId, productId, 2, 1_000))
                .isInstanceOf(SQLException.class);
    }

    // --- helpers: raw JDBC on purpose - Day 45 is where a connection pool shows up ---

    private long insertCustomer(String email, String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO customers (email, name) VALUES (?, ?) RETURNING id")) {
            ps.setString(1, email);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long insertProduct(String sku, String name, long priceCents) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO products (sku, name, price_cents) VALUES (?, ?, ?) RETURNING id")) {
            ps.setString(1, sku);
            ps.setString(2, name);
            ps.setLong(3, priceCents);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long insertOrder(long customerId, String status) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO orders (customer_id, status) VALUES (?, ?) RETURNING id")) {
            ps.setLong(1, customerId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void insertOrderItem(long orderId, long productId, int quantity, long unitPriceCents)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO order_items (order_id, product_id, quantity, unit_price_cents) "
                        + "VALUES (?, ?, ?, ?)")) {
            ps.setLong(1, orderId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            ps.setLong(4, unitPriceCents);
            ps.executeUpdate();
        }
    }
}
