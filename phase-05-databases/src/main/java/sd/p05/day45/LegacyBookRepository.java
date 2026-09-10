package sd.p05.day45;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * GIVEN, and the N+1 problem in its most natural habitat: an ORM-shaped loop that looks
 * completely innocent. One query fetches every book. Then, for EACH book, a second query fetches
 * its author - N books means N extra round trips, on top of the first query. 1,000 books is
 * 1,001 queries to render one page.
 *
 * <p>Nothing here is a typo or an obviously bad idea in isolation - "get the book, then get its
 * author" reads as perfectly reasonable code, one method at a time. The bug only becomes visible
 * at the level of "how many queries did this ONE page load actually issue" - which is exactly
 * why {@code Day45NPlusOneTest} counts queries directly rather than eyeballing the code.
 */
public final class LegacyBookRepository {

    public List<BookWithAuthor> findAllBooksWithAuthors(Connection conn) throws SQLException {
        List<BookWithAuthor> results = new ArrayList<>();

        try (PreparedStatement books = conn.prepareStatement(
                "SELECT title, author_id FROM books ORDER BY id")) {
            try (ResultSet bookRows = books.executeQuery()) {
                while (bookRows.next()) {
                    String title = bookRows.getString("title");
                    long authorId = bookRows.getLong("author_id");

                    try (PreparedStatement authorStmt = conn.prepareStatement(
                            "SELECT name FROM authors WHERE id = ?")) {
                        authorStmt.setLong(1, authorId);
                        try (ResultSet authorRow = authorStmt.executeQuery()) {
                            authorRow.next();
                            results.add(new BookWithAuthor(title, authorRow.getString("name")));
                        }
                    }
                }
            }
        }

        return results;
    }
}
