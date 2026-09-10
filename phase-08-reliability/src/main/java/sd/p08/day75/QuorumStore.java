package sd.p08.day75;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * TODO(day75): a leaderless replicated store, in the Dynamo style.
 *
 * <p>No leader. Every replica accepts reads and writes, and the client (or a coordinator) talks to
 * several of them and applies the quorum rule.
 *
 * <p>Compare with the alternatives:
 * <ul>
 *   <li><b>Leader-follower</b> (Postgres, MySQL, Kafka partitions) - all writes go to one node, so
 *       ordering is easy and the leader is a bottleneck and a failover event waiting to happen.</li>
 *   <li><b>Multi-leader</b> (cross-region active-active) - writes accepted in several places, so
 *       you get local write latency and you must resolve conflicts.</li>
 *   <li><b>Leaderless</b> (Dynamo, Cassandra, Riak) - no failover at all, because there is nothing
 *       to fail over. You pay with per-operation quorum arithmetic and conflict resolution.</li>
 * </ul>
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code write} - increment the global version, then write to every REACHABLE replica.
 *       Succeed if at least {@code writeQuorum} accepted; otherwise throw
 *       {@link QuorumNotMetException} <b>having still written to the ones that were reachable</b>.
 *       That partial write is not a bug - it is why a failed write can still be visible later, and
 *       why "the write failed" does not mean "the write did not happen".</li>
 *   <li>{@code read} - collect values from reachable replicas until you have {@code readQuorum}
 *       responses; return the highest-versioned one. Fewer than the quorum -> throw.</li>
 *   <li>{@code readRepair} - after a read, push the winning value to any replica that returned an
 *       older version. This is how a leaderless system heals: every read is an opportunity to
 *       converge.</li>
 *   <li>{@code fail} / {@code recover} - take a replica in or out of service.</li>
 * </ul>
 */
public final class QuorumStore {

    /** Thrown when not enough replicas responded to satisfy the configured quorum. */
    public static final class QuorumNotMetException extends RuntimeException {
        public QuorumNotMetException(String message) {
            super(message);
        }
    }

    private final QuorumConfig config;
    private final List<Map<String, VersionedValue>> replicas = new ArrayList<>();
    private final Set<Integer> failed = new HashSet<>();
    private long version;

    public QuorumStore(QuorumConfig config) {
        this.config = config;
        for (int i = 0; i < config.replicas(); i++) {
            replicas.add(new HashMap<>());
        }
    }

    public void write(String key, String value) {
        throw new UnsupportedOperationException("TODO(day75): write to all reachable, need W");
    }

    public Optional<String> read(String key) {
        throw new UnsupportedOperationException("TODO(day75): gather R responses, newest wins");
    }

    public int readRepair(String key) {
        throw new UnsupportedOperationException("TODO(day75): push the winner to stale replicas");
    }

    public void fail(int replica) {
        failed.add(replica);
    }

    public void recover(int replica) {
        failed.remove(replica);
    }

    /** GIVEN - what one replica holds, for tests that need to see divergence directly. */
    public Optional<VersionedValue> peek(int replica, String key) {
        return Optional.ofNullable(replicas.get(replica).get(key));
    }

    public QuorumConfig config() {
        return config;
    }
}
