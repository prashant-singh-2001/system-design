package sd.p05.day42;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ordered on purpose: this tells one continuous story (seed once, no index, add an index, add a
 * covering index) rather than three independent scenarios, because rebuilding 50,000 rows for
 * every test would make this day slower than the lesson is worth.
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Day42IndexingTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final int DISTINCT_USERS = 500;
    private static final int ROWS_PER_USER = 100;
    private static final int TARGET_USER_ID = 42;

    private static Connection connection;
    private static final IndexingLab LAB = new IndexingLab();

    private static String beforePlan;
    private static String afterIndexPlan;

    @BeforeAll
    static void setUpAndSeed() throws Exception {
        connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE events (
                        id BIGSERIAL PRIMARY KEY,
                        user_id INT NOT NULL,
                        event_type TEXT NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now()
                    )""");
        }

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO events (user_id, event_type) VALUES (?, ?)")) {
            for (int userId = 0; userId < DISTINCT_USERS; userId++) {
                for (int i = 0; i < ROWS_PER_USER; i++) {
                    insert.setInt(1, userId);
                    insert.setString(2, i % 2 == 0 ? "click" : "view");
                    insert.addBatch();
                }
            }
            insert.executeBatch();
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ANALYZE events");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    @Order(1)
    @DisplayName("before any index, the planner does a Seq Scan")
    void beforeIndexIsSeqScan() throws Exception {
        beforePlan = LAB.explainQuery(connection, TARGET_USER_ID);

        assertThat(LAB.classify(beforePlan)).isEqualTo(PlanKind.SEQ_SCAN);
    }

    @Test
    @Order(2)
    @DisplayName("adding an index switches the plan to an Index Scan, with the same results")
    void afterIndexIsIndexScan() throws Exception {
        LAB.createUserIdIndex(connection);

        afterIndexPlan = LAB.explainQuery(connection, TARGET_USER_ID);

        assertThat(LAB.classify(afterIndexPlan)).isEqualTo(PlanKind.INDEX_SCAN);
        assertThat(countMatchingRows(TARGET_USER_ID))
                .as("indexing must never change the ANSWER, only how fast Postgres gets there")
                .isEqualTo(ROWS_PER_USER);
    }

    @Test
    @Order(3)
    @DisplayName("a covering index lets Postgres answer without ever touching the table")
    void coveringIndexIsIndexOnlyScan() throws Exception {
        LAB.createCoveringIndex(connection);

        String coveringPlan = LAB.explainQuery(connection, TARGET_USER_ID);

        assertThat(LAB.classify(coveringPlan)).isEqualTo(PlanKind.INDEX_ONLY_SCAN);
        assertThat(countMatchingRows(TARGET_USER_ID))
                .as("still the same answer, now from an index-only scan")
                .isEqualTo(ROWS_PER_USER);
    }

    @Test
    @Order(4)
    @DisplayName("both plans get saved as evidence")
    void evidenceIsWritten(@org.junit.jupiter.api.io.TempDir Path tempDir) throws Exception {
        Path evidenceFile = tempDir.resolve("query-plans.md");

        LAB.writeEvidence(evidenceFile, beforePlan, afterIndexPlan);

        assertThat(Files.readString(evidenceFile))
                .contains("Seq Scan")
                .contains("Index Scan");
    }

    private long countMatchingRows(int userId) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT count(*) FROM events WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
