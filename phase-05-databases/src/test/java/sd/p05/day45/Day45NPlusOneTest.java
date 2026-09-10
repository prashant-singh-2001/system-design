package sd.p05.day45;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day45NPlusOneTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final int BOOK_COUNT = 20;

    @BeforeEach
    void seedData() throws Exception {
        try (Connection conn = rawConnection(); Statement statement = conn.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS books, authors");
            statement.execute("""
                    CREATE TABLE authors (id BIGSERIAL PRIMARY KEY, name TEXT NOT NULL)""");
            statement.execute("""
                    CREATE TABLE books (
                        id BIGSERIAL PRIMARY KEY,
                        title TEXT NOT NULL,
                        author_id BIGINT NOT NULL REFERENCES authors(id)
                    )""");

            try (PreparedStatement insertAuthor = conn.prepareStatement(
                    "INSERT INTO authors (name) VALUES (?)")) {
                for (int i = 0; i < 5; i++) {
                    insertAuthor.setString(1, "Author " + i);
                    insertAuthor.addBatch();
                }
                insertAuthor.executeBatch();
            }
            try (PreparedStatement insertBook = conn.prepareStatement(
                    "INSERT INTO books (title, author_id) VALUES (?, (?::bigint))")) {
                for (int i = 0; i < BOOK_COUNT; i++) {
                    insertBook.setString(1, "Book " + i);
                    insertBook.setLong(2, (i % 5) + 1);
                    insertBook.addBatch();
                }
                insertBook.executeBatch();
            }
        }
    }

    private Connection rawConnection() throws Exception {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @Test
    @DisplayName("the legacy repository issues exactly 1 + N queries for N books")
    void legacyRepositoryHasNPlusOneQueries() throws Exception {
        AtomicInteger queryCount = new AtomicInteger(0);
        try (Connection raw = rawConnection()) {
            Connection counted = CountingConnection.wrap(raw, queryCount);

            List<BookWithAuthor> books = new LegacyBookRepository().findAllBooksWithAuthors(counted);

            assertThat(books).hasSize(BOOK_COUNT);
            assertThat(queryCount)
                    .as("one query for all books, plus one per-book author lookup")
                    .hasValue(1 + BOOK_COUNT);
        }
    }

    @Test
    @DisplayName("the fixed repository issues exactly ONE query, with identical results")
    void fixedRepositoryHasOneQuery() throws Exception {
        AtomicInteger legacyCount = new AtomicInteger(0);
        AtomicInteger fixedCount = new AtomicInteger(0);
        List<BookWithAuthor> legacyResult;
        List<BookWithAuthor> fixedResult;

        try (Connection raw = rawConnection()) {
            legacyResult = new LegacyBookRepository()
                    .findAllBooksWithAuthors(CountingConnection.wrap(raw, legacyCount));
        }
        try (Connection raw = rawConnection()) {
            fixedResult = new BookRepository()
                    .findAllBooksWithAuthors(CountingConnection.wrap(raw, fixedCount));
        }

        assertThat(fixedCount).as("a single JOIN, regardless of how many books exist").hasValue(1);
        assertThat(fixedResult)
                .as("the fix must not change a single answer, only how many round trips it took")
                .containsExactlyElementsOf(legacyResult);
    }
}
