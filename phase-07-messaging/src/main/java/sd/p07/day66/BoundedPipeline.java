package sd.p07.day66;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO(day66): backpressure - what happens when a fast producer meets a slow consumer.
 *
 * <p>An <b>unbounded</b> queue looks like the safe choice and is the dangerous one. Producing
 * faster than you consume means the queue grows without limit, and the failure mode is not a
 * clean error - it is memory climbing for twenty minutes, then GC thrashing, then an
 * OutOfMemoryError that takes the whole process down, including the healthy work it was doing.
 * You have converted a throughput problem into an availability incident.
 *
 * <p>A <b>bounded</b> queue forces the decision to be made explicitly and early, while you still
 * have options. The queue is your buffer for bursts; the bound is your promise about the worst
 * case. Little's Law (Day 3) tells you what depth to pick: at 1,000 items/second and 50 ms of
 * processing you need 50 in flight, so a bound of a few hundred absorbs bursts without hiding a
 * sustained mismatch.
 *
 * <p>That is the real point. <b>A queue absorbs bursts. It cannot fix a rate mismatch.</b> If
 * production exceeds consumption on average, no buffer size saves you - it only changes how long
 * you wait before finding out.
 *
 * <p>Implement {@code run}: start a consumer thread that takes from the queue, sleeps
 * {@code processingTime}, and counts; then offer {@code itemCount} items from the calling thread
 * according to {@link OverflowPolicy}:
 * <ul>
 *   <li>{@code BLOCK} - {@code queue.put(...)}, which blocks until there is room</li>
 *   <li>{@code DROP_NEWEST} - {@code queue.offer(...)}; a false return counts as a drop</li>
 *   <li>{@code DROP_OLDEST} - if {@code offer} fails, {@code poll()} one off the front (count it
 *       as a drop) and offer again</li>
 * </ul>
 *
 * <p>Track the deepest the queue ever got, then signal the consumer to stop, join it, and return
 * a {@link BackpressureResult}.
 */
public final class BoundedPipeline {

    private BoundedPipeline() {
    }

    public static BackpressureResult run(int itemCount, int queueCapacity,
                                         Duration processingTime, OverflowPolicy policy)
            throws InterruptedException {
        throw new UnsupportedOperationException("TODO(day66): implement the bounded pipeline");
    }

    /**
     * TODO(day66): the before-picture - an UNBOUNDED queue with no consumer at all.
     *
     * <p>Offer {@code itemCount} items to a {@code LinkedBlockingQueue} with no consumer running,
     * and return the final queue depth. Nothing fails. Nothing warns you. The queue simply grows,
     * which in production means memory climbing until the process dies.
     *
     * <p>Every item is "accepted", and that is exactly the problem: accepting work you cannot do
     * is a lie you tell yourself in units of megabytes.
     */
    public static int unboundedGrowth(int itemCount) {
        throw new UnsupportedOperationException("TODO(day66): watch it grow without complaint");
    }
}
