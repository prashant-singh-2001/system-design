package sd.p09.capstone.adapter;

import sd.p09.capstone.domain.LinkRepository;
import sd.p09.capstone.domain.ShortLink;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO(day88): the storage adapter - real Postgres, real SQL.
 *
 * <p>Everything Phase 5 taught applies here, and the schema below encodes it:
 * <ul>
 *   <li>{@code code} is the primary key, because the redirect lookup is the hot path and it should
 *       be a single index probe.</li>
 *   <li>{@code target_url} is UNIQUE, which makes the idempotency rule a database guarantee rather
 *       than an application convention - and gives the dedupe lookup an index for free.</li>
 *   <li>{@code clicks} is updated with a single {@code UPDATE ... RETURNING}, not a read-modify-
 *       write. Day 4's lost update, avoided by never bringing the value into Java at all.</li>
 * </ul>
 *
 * <p>Implement the four methods. Count queries so the caching tests on Day 89 can prove the cache
 * is working - a cache you cannot measure is a cache you cannot trust.
 */
public final class PostgresLinkRepository implements LinkRepository {

    public static final String SCHEMA = """
            CREATE TABLE IF NOT EXISTS links (
                code       TEXT PRIMARY KEY,
                target_url TEXT   NOT NULL UNIQUE,
                created_at TIMESTAMPTZ NOT NULL,
                clicks     BIGINT NOT NULL DEFAULT 0
            );
            """;

    private final Connection connection;
    private final AtomicInteger queries = new AtomicInteger();

    public PostgresLinkRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(ShortLink link) {
        throw new UnsupportedOperationException("TODO(day88): insert the link");
    }

    @Override
    public Optional<ShortLink> findByCode(String code) {
        throw new UnsupportedOperationException("TODO(day88): the hot path - one index probe");
    }

    @Override
    public Optional<ShortLink> findByTargetUrl(String targetUrl) {
        throw new UnsupportedOperationException("TODO(day88): the dedupe lookup");
    }

    @Override
    public Optional<Long> incrementClicks(String code) {
        throw new UnsupportedOperationException(
                "TODO(day88): UPDATE ... SET clicks = clicks + 1 ... RETURNING clicks");
    }

    /** How many SQL statements this repository has executed. */
    public int queries() {
        return queries.get();
    }

    public void resetQueryCount() {
        queries.set(0);
    }

    // ---------------------------------------------------------------- given

    static ShortLink readLink(ResultSet rs) throws SQLException {
        return new ShortLink(
                rs.getString("code"),
                rs.getString("target_url"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getLong("clicks"));
    }

    PreparedStatement prepare(String sql) throws SQLException {
        queries.incrementAndGet();
        return connection.prepareStatement(sql);
    }
}
