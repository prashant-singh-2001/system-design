package sd.p08.day77;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO(day77): a lease-based distributed lock, and why the lock alone is not enough.
 *
 * <p>A distributed lock cannot be held indefinitely, because the holder might die and never
 * release it. So locks are <b>leases</b> - they expire. And that is precisely where the danger
 * enters.
 *
 * <p>The failure mode, in order:
 * <ol>
 *   <li>Client A acquires a 30-second lease.</li>
 *   <li>Client A stops the world - a long GC pause, a hypervisor stall, a network partition. It
 *       is not dead, just frozen, and it has no idea any time has passed.</li>
 *   <li>The lease expires. Client B legitimately acquires it.</li>
 *   <li>Client A resumes, still believing it holds the lock, and writes.</li>
 * </ol>
 *
 * <p>Two clients now believe they hold the same lock, and no amount of care in the lock service
 * prevents it. Redlock's much-discussed weakness is exactly this: <b>no lock service can stop a
 * frozen client from waking up and acting.</b> The problem is not the lock, it is the gap between
 * checking and acting.
 *
 * <p>The fix is a <b>fencing token</b>: a number that increases with every acquisition. The client
 * passes it to the resource, and the resource refuses any token lower than the highest it has
 * seen. Client A wakes up holding token 33, the storage has already accepted token 34, and A's
 * write is rejected. Safety is enforced at the <b>resource</b>, which is the only place that can
 * actually enforce it.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code tryAcquire} - grant if the resource is free or its lease has expired; issue the
 *       next token. Return empty if it is validly held by somebody else.</li>
 *   <li>{@code release} - only the current owner may release. Releasing someone else's lock is a
 *       classic bug: your lease expired, another client took it, and your "cleanup" frees theirs.</li>
 *   <li>{@code isHeldBy} - held by this owner and not yet expired.</li>
 * </ul>
 *
 * <p>Take a {@link Clock} so expiry is testable without waiting.
 */
public final class DistributedLock {

    private final Map<String, Lease> leases = new HashMap<>();
    private final Clock clock;
    private long nextToken = 1;

    public DistributedLock(Clock clock) {
        this.clock = clock;
    }

    public Optional<Lease> tryAcquire(String resource, String owner, Duration leaseDuration) {
        throw new UnsupportedOperationException("TODO(day77): grant if free or expired");
    }

    public boolean release(String resource, String owner) {
        throw new UnsupportedOperationException("TODO(day77): only the current owner may release");
    }

    public boolean isHeldBy(String resource, String owner) {
        throw new UnsupportedOperationException("TODO(day77): held, and not expired");
    }
}
