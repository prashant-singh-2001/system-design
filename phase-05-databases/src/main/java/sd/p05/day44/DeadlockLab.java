package sd.p05.day44;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * TODO(day44): cause a real deadlock on purpose, then fix it with lock ordering - the actual
 * production technique, not a metaphor for one.
 *
 * <p>All three methods operate on an {@code accounts(id, balance)} table.
 *
 * <p><b>{@code lockRowForUpdate(conn, accountId)}</b> - run
 * {@code SELECT balance FROM accounts WHERE id = ? FOR UPDATE} and return the balance.
 * {@code FOR UPDATE} takes a row-level write lock that is held until the transaction ends -
 * another transaction's own {@code FOR UPDATE} (or plain {@code UPDATE}) on the SAME row blocks
 * until this one commits or rolls back. A plain, lock-free {@code SELECT} on that row does NOT
 * block, no matter what - that is MVCC's whole point, and the brief's tests check it directly.
 *
 * <p><b>{@code naiveTransfer(conn, fromId, toId, amountCents, afterFirstLock)}</b> - the
 * "before" shape: lock {@code fromId} via {@code lockRowForUpdate}, invoke
 * {@code afterFirstLock.run()} if it is non-null (a synchronization hook a test uses to force a
 * deterministic interleaving - production code would simply pass {@code null}), THEN lock
 * {@code toId} the same way, then apply the actual balance changes and commit. Called from two
 * threads transferring in OPPOSITE directions at the same time, this deadlocks - Postgres
 * detects the cycle and kills one side with a {@code SQLException} whose
 * {@code getSQLState()} is {@code "40P01"}.
 *
 * <p><b>{@code orderedTransfer(...)}</b> - identical operation, ONE change: lock
 * {@code Math.min(fromId, toId)} first and {@code Math.max(fromId, toId)} second, REGARDLESS of
 * which direction the transfer is going, then apply the debit/credit according to the ACTUAL
 * {@code fromId}/{@code toId}. Every transaction now acquires locks in the same global order, so
 * the circular-wait condition a deadlock requires can never form.
 */
public final class DeadlockLab {

    public int lockRowForUpdate(Connection conn, int accountId) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day44): SELECT balance ... FOR UPDATE, return the balance");
    }

    public void naiveTransfer(Connection conn, int fromId, int toId, int amountCents,
                               Runnable afterFirstLock) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day44): lock fromId, run the hook, lock toId, apply changes, commit");
    }

    public void orderedTransfer(Connection conn, int fromId, int toId, int amountCents,
                                 Runnable afterFirstLock) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day44): lock min(fromId,toId) then max(fromId,toId), then apply, commit");
    }
}
