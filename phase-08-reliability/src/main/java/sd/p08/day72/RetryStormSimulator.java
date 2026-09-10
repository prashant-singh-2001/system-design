package sd.p08.day72;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * TODO(day72): measure the storm, rather than taking it on faith.
 *
 * <p>Model a moment where {@code clientCount} clients all fail at time zero and each retries
 * {@code attempts} times. For every client and attempt, accumulate the delay from the given
 * strategy, bucket the resulting instant into a 100 ms slot, and count how many retries land in
 * each slot.
 *
 * <p>Return the busiest slot's count - the <b>peak instantaneous load</b> the dependency actually
 * sees. That single number decides whether it recovers or falls over, and it is invisible if you
 * only look at the total retry count.
 *
 * <p>Run it with exponential backoff and then with full jitter. Same total retries, same average
 * rate, wildly different peak - the entire argument for jitter, in one measurement.
 */
public final class RetryStormSimulator {

    private RetryStormSimulator() {
    }

    public enum Strategy {
        EXPONENTIAL, FULL_JITTER
    }

    public static int peakConcurrentRetries(int clientCount, int attempts, Duration base,
                                            Duration max, Strategy strategy,
                                            RandomGenerator random) {
        throw new UnsupportedOperationException("TODO(day72): bucket the retries, return the peak");
    }
}
