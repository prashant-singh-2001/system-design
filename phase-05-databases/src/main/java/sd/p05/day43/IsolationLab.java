package sd.p05.day43;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * TODO(day43): reproduce three classic anomalies on two REAL, separate JDBC connections to the
 * same database - no threads needed, since you fully control the order two transactions'
 * statements are sent in by simply calling them in that order on this one test thread.
 *
 * <p>All three methods take two connections with {@code setAutoCommit(false)} already applied by
 * the caller, and are responsible for calling {@code commit()} or {@code rollback()} on both
 * before returning.
 *
 * <p><b>{@code dirtyReadOccurs(writer, reader)}</b> - set both connections' transaction
 * isolation to {@code Connection.TRANSACTION_READ_UNCOMMITTED}. {@code writer} updates
 * {@code accounts.balance} for id 1 but does NOT commit yet. {@code reader} then reads that same
 * row. {@code writer} rolls back. Return whether {@code reader} saw the UNCOMMITTED (and now
 * rolled-back) value. Spoiler, and the actual point of the exercise: in Postgres this is always
 * {@code false}, because Postgres silently upgrades {@code READ UNCOMMITTED} to
 * {@code READ COMMITTED} - it has no dirty-read isolation level at all, no matter what you ask
 * for.
 *
 * <p><b>{@code nonRepeatableReadOccurs(reader, writer, isolationLevel)}</b> - set {@code
 * reader}'s isolation to the given level (use the {@code Connection.TRANSACTION_*} constants) and
 * begin its transaction with a first read of {@code accounts.balance} for id 1. {@code writer}
 * updates that row and COMMITS. {@code reader} reads the SAME row again, in the SAME still-open
 * transaction. Return whether the two reads differ. Then commit or rollback both.
 *
 * <p><b>{@code phantomReadOccurs(reader, writer, isolationLevel)}</b> - same shape, but
 * {@code reader} runs {@code SELECT count(*) FROM accounts WHERE balance > 50} both times, and
 * {@code writer} INSERTS a new row with {@code balance > 50} and commits in between.
 */
public final class IsolationLab {

    public boolean dirtyReadOccurs(Connection writer, Connection reader) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day43): writer updates uncommitted, reader reads, writer rolls back");
    }

    public boolean nonRepeatableReadOccurs(Connection reader, Connection writer, int isolationLevel)
            throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day43): reader reads twice around writer's committed update in between");
    }

    public boolean phantomReadOccurs(Connection reader, Connection writer, int isolationLevel)
            throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day43): reader counts twice around writer's committed insert in between");
    }
}
