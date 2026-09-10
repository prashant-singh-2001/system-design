package sd.p05.day43;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day43IsolationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static Connection setupConnection;
    private final IsolationLab lab = new IsolationLab();

    @BeforeAll
    static void createSchema() throws Exception {
        setupConnection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        try (Statement statement = setupConnection.createStatement()) {
            statement.execute("CREATE TABLE accounts (id INT PRIMARY KEY, balance INT NOT NULL)");
        }
    }

    @AfterAll
    static void closeSetupConnection() throws Exception {
        setupConnection.close();
    }

    private Connection connA;
    private Connection connB;

    @BeforeEach
    void resetDataAndOpenConnections() throws Exception {
        try (Statement statement = setupConnection.createStatement()) {
            statement.execute("TRUNCATE accounts");
            statement.execute("INSERT INTO accounts (id, balance) VALUES (1, 100)");
        }
        connA = newConnection();
        connB = newConnection();
    }

    @AfterEach
    void closeConnections() throws Exception {
        connA.close();
        connB.close();
    }

    private Connection newConnection() throws Exception {
        Connection conn = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        conn.setAutoCommit(false);
        return conn;
    }

    @Test
    @DisplayName("Postgres has no true READ UNCOMMITTED - a dirty read never happens")
    void dirtyReadNeverHappensInPostgres() throws Exception {
        boolean dirtyReadOccurred = lab.dirtyReadOccurs(connA, connB);

        assertThat(dirtyReadOccurred)
                .as("Postgres silently treats READ UNCOMMITTED as READ COMMITTED")
                .isFalse();
    }

    @Test
    @DisplayName("READ COMMITTED allows a non-repeatable read")
    void nonRepeatableReadUnderReadCommitted() throws Exception {
        boolean occurred = lab.nonRepeatableReadOccurs(
                connA, connB, Connection.TRANSACTION_READ_COMMITTED);

        assertThat(occurred).isTrue();
    }

    @Test
    @DisplayName("REPEATABLE READ prevents a non-repeatable read")
    void noNonRepeatableReadUnderRepeatableRead() throws Exception {
        boolean occurred = lab.nonRepeatableReadOccurs(
                connA, connB, Connection.TRANSACTION_REPEATABLE_READ);

        assertThat(occurred)
                .as("a snapshot taken at the start of the transaction must not change mid-transaction")
                .isFalse();
    }

    @Test
    @DisplayName("READ COMMITTED allows a phantom read")
    void phantomReadUnderReadCommitted() throws Exception {
        boolean occurred = lab.phantomReadOccurs(
                connA, connB, Connection.TRANSACTION_READ_COMMITTED);

        assertThat(occurred).isTrue();
    }

    @Test
    @DisplayName("Postgres's REPEATABLE READ also prevents phantom reads - stricter than the SQL standard requires")
    void noPhantomReadUnderRepeatableRead() throws Exception {
        boolean occurred = lab.phantomReadOccurs(
                connA, connB, Connection.TRANSACTION_REPEATABLE_READ);

        assertThat(occurred)
                .as("Postgres REPEATABLE READ is snapshot-based, which blocks phantoms too")
                .isFalse();
    }
}
