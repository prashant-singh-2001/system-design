package sd.p03.day24;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * GIVEN - stands in for a real network call that is both slow and unreliable, which is to say,
 * a completely typical downstream dependency.
 *
 * <p>Fails on every call until {@code failuresBeforeSuccess} calls have happened, then succeeds
 * on every call after that - deterministic, so a test can predict exactly how many attempts a
 * retry needs without any actual timing or randomness involved.
 */
public final class FlakyRemoteService implements SlowService {

    private final int failuresBeforeSuccess;
    private final AtomicInteger callCount = new AtomicInteger(0);

    public FlakyRemoteService(int failuresBeforeSuccess) {
        this.failuresBeforeSuccess = failuresBeforeSuccess;
    }

    @Override
    public String fetch(String key) {
        int callNumber = callCount.incrementAndGet();
        if (callNumber <= failuresBeforeSuccess) {
            throw new RuntimeException("simulated failure #" + callNumber + " for " + key);
        }
        return "value-for-" + key;
    }

    /** How many times {@link #fetch} has actually been invoked - the fact a test checks. */
    public int callCount() {
        return callCount.get();
    }
}
