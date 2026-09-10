package sd.p05.day42;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * TODO(day42): make a slow query fast, and PROVE it with the query planner's own output rather
 * than a stopwatch and a guess.
 *
 * <p>{@code explainQuery(conn, userId)} - run
 * {@code EXPLAIN (ANALYZE, FORMAT TEXT) SELECT event_type FROM events WHERE user_id = ?}
 * with the given {@code userId} bound, and concatenate every row of the plan output (one
 * {@code EXPLAIN} statement returns its plan as a multi-row result set, one line of text per
 * row) into a single {@code String}, newline-separated. {@code ANALYZE} matters here - plain
 * {@code EXPLAIN} shows what Postgres THINKS it will do; {@code EXPLAIN ANALYZE} actually runs
 * the query and shows real timings and row counts.
 *
 * <p>{@code createUserIdIndex(conn)} - {@code CREATE INDEX ON events(user_id)}, then
 * {@code ANALYZE events} so the planner's statistics are fresh enough to actually consider using
 * the new index (a stale planner will happily ignore a brand-new index).
 *
 * <p>{@code createCoveringIndex(conn)} - drop the plain index and replace it with one that
 * INCLUDES {@code event_type}: {@code CREATE INDEX ON events(user_id) INCLUDE (event_type)},
 * then {@code VACUUM ANALYZE events}. The {@code VACUUM} matters as much as the {@code ANALYZE}
 * here - an INDEX ONLY SCAN needs the visibility map to know every row is definitely visible to
 * every transaction, and only a vacuum brings that map up to date.
 *
 * <p>{@code classify(planText)} - {@code SEQ_SCAN} if the plan mentions {@code "Seq Scan"},
 * {@code INDEX_ONLY_SCAN} if it mentions {@code "Index Only Scan"}, {@code INDEX_SCAN} if it
 * mentions {@code "Index Scan"} (check for "Index Only" FIRST - it also contains the substring
 * "Index Scan"), otherwise {@code OTHER}.
 *
 * <p>{@code writeEvidence(file, beforePlan, afterPlan)} - write both plans to a small markdown
 * file: a heading, the before plan in a fenced code block, another heading, the after plan in
 * another fenced code block. This is your "both plans saved as evidence" deliverable.
 */
public final class IndexingLab {

    public String explainQuery(Connection conn, int userId) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day42): run EXPLAIN (ANALYZE, FORMAT TEXT) ... and concatenate the plan rows");
    }

    public void createUserIdIndex(Connection conn) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day42): CREATE INDEX ON events(user_id); ANALYZE events;");
    }

    public void createCoveringIndex(Connection conn) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day42): drop the plain index, create one INCLUDE-ing event_type, VACUUM ANALYZE");
    }

    public PlanKind classify(String planText) {
        throw new UnsupportedOperationException(
                "TODO(day42): check for Index Only Scan before Index Scan, then Seq Scan, else OTHER");
    }

    public void writeEvidence(Path file, String beforePlan, String afterPlan) throws IOException {
        throw new UnsupportedOperationException(
                "TODO(day42): write both plans to a small markdown file at `file`");
    }
}
