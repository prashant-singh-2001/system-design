package sd.p05.day45;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * TODO(day45): the same result as {@link LegacyBookRepository}, in ONE query instead of
 * {@code 1 + N}.
 *
 * <p>{@code SELECT b.title, a.name FROM books b JOIN authors a ON a.id = b.author_id ORDER BY
 * b.id} - a single round trip fetches every book already paired with its author's name. Build
 * the {@link BookWithAuthor} list from that one result set.
 */
public final class BookRepository {

    public List<BookWithAuthor> findAllBooksWithAuthors(Connection conn) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day45): one JOIN query, build the list directly from its result set");
    }
}
