package sd.p05.day46;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO(day46): the SAME feature - a user's profile plus their 5 most recent orders in one read -
 * modelled and fetched two ways against the SAME underlying facts, so you can compare them
 * directly rather than take the trade-off on faith.
 *
 * <p><b>The normalized (relational) shape</b> is what Day 41 already trained you to reach for:
 * {@code users(id, name, email)} and {@code orders(id, user_id, total_cents, created_at)}, joined
 * or queried in two steps. {@code fetchViaJoin(conn, userId)}: one query for the user, one query
 * for their 5 most recent orders ({@code ORDER BY id DESC LIMIT 5} - order ids are assigned in
 * increasing recency order in this exercise's data, which is a simpler and more deterministic
 * recency signal here than a timestamp column would be), assembled into a {@link UserProfile}.
 *
 * <p><b>The wide-row (NoSQL-style) shape</b> denormalizes on purpose: one table,
 * {@code user_profile_view(user_id, name, email, recent_order_ids, recent_order_totals_cents)},
 * where the last two columns are native Postgres {@code INTEGER[]} / {@code BIGINT[]} arrays -
 * a bounded, embedded collection living directly on the row, the same shape a wide-column store
 * would call a "list" or "set" column. {@code fetchViaDenormalizedView(conn, userId)}: exactly
 * ONE query, reading the arrays back via {@code ResultSet.getArray(...)}.
 *
 * <p>The trade shows up on the WRITE side, and this is the part a read-only comparison misses:
 * {@code recordOrder(conn, userId, orderId, totalCents)} has to do TWO things now, not one -
 * insert into the normalized {@code orders} table (the source of truth), AND update
 * {@code user_profile_view} to keep the denormalized copy in sync. Prepend the new order to the
 * FRONT of both arrays and keep only the first 5:
 * {@code (ARRAY[?::bigint] || recent_order_ids)[1:5]} - Postgres array concatenation with
 * {@code ||}, then a slice {@code [1:5]} for "the first five elements". The explicit
 * {@code ::bigint} cast matters: without it, the driver cannot infer a type for a lone
 * {@code ARRAY[?]} parameter and the concatenation fails. Do the identical operation to
 * {@code recent_order_totals_cents} with the matching total.
 */
public final class UserProfileService {

    public UserProfile fetchViaJoin(Connection conn, long userId) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day46): one query for the user, one for their 5 most recent orders");
    }

    public UserProfile fetchViaDenormalizedView(Connection conn, long userId) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day46): one query against user_profile_view, reading the array columns");
    }

    public void recordOrder(Connection conn, long userId, long orderId, long totalCents)
            throws SQLException {
        throw new UnsupportedOperationException(
                "TODO(day46): insert into orders, then prepend-and-trim both arrays on the view");
    }
}
