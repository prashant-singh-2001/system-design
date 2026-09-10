package sd.p08.day71;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO(day71): make CAP concrete by living through a partition.
 *
 * <p>Two replicas of the same key-value store. Normally they replicate to each other. Then the
 * network splits them, and every design decision in distributed systems shows up in what happens
 * next.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code write} - while healthy, write locally and replicate to the peer. During a
 *       partition, behaviour depends on {@link PartitionStrategy}:
 *       <ul>
 *         <li><b>CP</b>: the MAJORITY side accepts (and cannot replicate, which is fine - it will
 *             catch the peer up on heal). The MINORITY side <b>throws</b>
 *             {@link UnavailableException} rather than accept a write it cannot make safe.</li>
 *         <li><b>AP</b>: both sides accept locally and diverge. Nobody is refused.</li>
 *       </ul>
 *   </li>
 *   <li>{@code read} - always answers from local state. Under AP that means the minority side can
 *       return data it knows may be stale, which is the honest cost of staying up.</li>
 *   <li>{@code partition} / {@code heal} - flip the flag. On heal, reconcile with
 *       last-write-wins by the write counter.</li>
 * </ul>
 *
 * <p>Last-write-wins is deliberately the simplest reconciliation and deliberately lossy: one
 * side's write is silently discarded. Real AP systems use vector clocks, CRDTs, or hand the
 * conflict to the application - because "silently discard one customer's change" is rarely an
 * acceptable product decision, even when it is an easy engineering one.
 */
public final class PartitionedStore {

    /** Thrown by a CP system that would rather refuse than risk divergence. */
    public static final class UnavailableException extends RuntimeException {
        public UnavailableException(String message) {
            super(message);
        }
    }

    private record Versioned(String value, long version) {
    }

    private final Map<String, Versioned> majority = new HashMap<>();
    private final Map<String, Versioned> minority = new HashMap<>();
    private final PartitionStrategy strategy;
    private boolean partitioned;
    private long clock;

    public PartitionedStore(PartitionStrategy strategy) {
        this.strategy = strategy;
    }

    public void write(ReplicaSide side, String key, String value) {
        throw new UnsupportedOperationException("TODO(day71): CP refuses on the minority side");
    }

    public Optional<String> read(ReplicaSide side, String key) {
        throw new UnsupportedOperationException("TODO(day71): always answer locally");
    }

    public void partition() {
        partitioned = true;
    }

    /** TODO(day71): heal the split and reconcile, last write wins by version. */
    public void heal() {
        throw new UnsupportedOperationException("TODO(day71): merge both sides");
    }

    public boolean isPartitioned() {
        return partitioned;
    }

    public PartitionStrategy strategy() {
        return strategy;
    }
}
