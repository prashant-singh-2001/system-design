package sd.p08.day77;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sd.p08.support.MutableClock;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class Day77FencingTest {

    private static final Duration LEASE = Duration.ofSeconds(30);

    private MutableClock clock;
    private DistributedLock lock;

    @BeforeEach
    void setUp() {
        clock = MutableClock.startingAt("2026-03-01T12:00:00Z");
        lock = new DistributedLock(clock);
    }

    @Test
    @DisplayName("a free resource can be locked")
    void acquireFree() {
        assertThat(lock.tryAcquire("job", "client-a", LEASE)).isPresent();
        assertThat(lock.isHeldBy("job", "client-a")).isTrue();
    }

    @Test
    @DisplayName("a held lock excludes everybody else")
    void mutualExclusion() {
        lock.tryAcquire("job", "client-a", LEASE);

        assertThat(lock.tryAcquire("job", "client-b", LEASE)).isEmpty();
        assertThat(lock.isHeldBy("job", "client-b")).isFalse();
    }

    @Test
    @DisplayName("an expired lease is available again - a dead holder must not block forever")
    void leasesExpire() {
        lock.tryAcquire("job", "client-a", LEASE);

        clock.advance(Duration.ofSeconds(31));

        assertThat(lock.isHeldBy("job", "client-a")).isFalse();
        assertThat(lock.tryAcquire("job", "client-b", LEASE)).isPresent();
    }

    @Test
    @DisplayName("only the owner may release - releasing someone else's lock is a classic bug")
    void onlyOwnerReleases() {
        lock.tryAcquire("job", "client-a", LEASE);

        assertThat(lock.release("job", "client-b"))
                .as("""
                        Your lease expired, another client took the lock, and your 'cleanup' would
                        have freed theirs - while they were mid-write.""")
                .isFalse();
        assertThat(lock.isHeldBy("job", "client-a")).isTrue();

        assertThat(lock.release("job", "client-a")).isTrue();
        assertThat(lock.tryAcquire("job", "client-b", LEASE)).isPresent();
    }

    @Test
    @DisplayName("tokens increase with every acquisition")
    void tokensAreMonotonic() {
        long first = lock.tryAcquire("job", "client-a", LEASE).orElseThrow().token();
        lock.release("job", "client-a");
        long second = lock.tryAcquire("job", "client-b", LEASE).orElseThrow().token();

        assertThat(second).isGreaterThan(first);
    }

    @Test
    @DisplayName("THE scenario: a frozen client wakes up and its stale write is rejected")
    void fencingStopsTheZombieWriter() {
        FencedResource storage = new FencedResource();

        // Client A takes the lock and begins work.
        Lease a = lock.tryAcquire("job", "client-a", LEASE).orElseThrow();

        // A stops the world: a long GC pause. It is not dead, just frozen, and it has no idea
        // any time has passed. Its lease expires while it is suspended.
        clock.advance(Duration.ofSeconds(31));

        // B legitimately acquires the lock and writes.
        Lease b = lock.tryAcquire("job", "client-b", LEASE).orElseThrow();
        assertThat(storage.write(b.token(), "written by B")).isTrue();

        // A resumes, still believing it holds the lock.
        boolean accepted = storage.write(a.token(), "written by A - stale!");

        assertThat(accepted)
                .as("""
                        No lock service can stop a frozen client waking up and acting - this is
                        Redlock's real weakness. Safety has to be enforced at the RESOURCE, which
                        is the only component that sees the writes and can order them.""")
                .isFalse();
        assertThat(storage.writes()).containsExactly("written by B");
        assertThat(storage.rejectedWrites()).isEqualTo(1);
    }

    @Test
    @DisplayName("without fencing, that same write would have been accepted")
    void theBugYouAvoided() {
        FencedResource unfenced = new FencedResource();

        // Simulating no fencing at all: every writer passes the same token.
        assertThat(unfenced.write(1, "B's work")).isTrue();
        assertThat(unfenced.write(1, "A's stale work"))
                .as("""
                        With no increasing token there is nothing to compare, so the zombie write
                        lands on top of the legitimate one. The lock did its job; the design still
                        corrupted the data.""")
                .isTrue();

        assertThat(unfenced.writes()).containsExactly("B's work", "A's stale work");
    }

    @Test
    @DisplayName("the same token can write repeatedly - fencing bounds staleness, not throughput")
    void sameTokenMayWriteAgain() {
        FencedResource storage = new FencedResource();
        Lease lease = lock.tryAcquire("job", "client-a", LEASE).orElseThrow();

        assertThat(storage.write(lease.token(), "one")).isTrue();
        assertThat(storage.write(lease.token(), "two")).isTrue();
        assertThat(storage.write(lease.token(), "three")).isTrue();

        assertThat(storage.writes()).hasSize(3);
        assertThat(storage.rejectedWrites()).isZero();
    }

    @Test
    @DisplayName("the high-water mark only ever rises")
    void highWaterMark() {
        FencedResource storage = new FencedResource();

        storage.write(5, "a");
        storage.write(9, "b");
        storage.write(7, "stale");

        assertThat(storage.highestTokenSeen()).isEqualTo(9);
        assertThat(storage.writes()).containsExactly("a", "b");
    }

    @Test
    @DisplayName("different resources are locked independently")
    void independentResources() {
        lock.tryAcquire("job-a", "client-1", LEASE);

        assertThat(lock.tryAcquire("job-b", "client-2", LEASE)).isPresent();
    }
}
