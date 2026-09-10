package sd.p05.day45;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * TODO(day45): run a fixed workload through a real connection pool and MEASURE how many
 * connections were actually in use at once - a deterministic, hardware-independent way to prove
 * a pool's size cap is real, instead of racing wall-clock timings that vary machine to machine.
 *
 * <p>{@code runWorkload(dataSource, concurrentClients, queriesPerClient)}: start
 * {@code concurrentClients} threads (a fixed thread pool sized to match is fine). Each thread
 * runs {@code queriesPerClient} times: borrow a connection from {@code dataSource}
 * ({@code try-with-resources} on the borrowed {@code Connection} is enough - closing a pooled
 * connection returns it to the pool rather than actually closing it), THEN start the clock,
 * run a query that takes a small but real amount of time -
 * {@code SELECT pg_sleep(0.05)} is a clean way to simulate "real work" deterministically -
 * and stop the clock. Record that start/end pair as a {@link QueryTiming}.
 *
 * <p>The order there matters: start timing AFTER the connection is acquired, not before. Time
 * spent WAITING for a free connection is not time spent USING one, and folding wait time into
 * the measurement would make it look like more queries overlapped than the pool ever actually
 * allowed to run at once - which defeats the entire point of measuring overlap instead of guessing
 * from wall-clock time.
 *
 * <p>Collect every timing from every thread into one thread-safe list ({@code
 * CopyOnWriteArrayList} is a reasonable choice) and return it once every thread has finished.
 *
 * <p>{@link #maxConcurrentOverlap} is given - a classic sweep-line: turn every timing into a
 * {@code +1} at its start and a {@code -1} at its end, sort all those events by time, and track
 * the running total's peak. That peak is the maximum number of queries that were EVER actually
 * in flight at the same instant - which can never exceed the pool's {@code maximumPoolSize}, no
 * matter how many client threads are contending for it.
 */
public final class PoolSizingBenchmark {

    public List<QueryTiming> runWorkload(DataSource dataSource, int concurrentClients,
                                          int queriesPerClient) throws InterruptedException {
        throw new UnsupportedOperationException(
                "TODO(day45): concurrentClients threads, each timing queriesPerClient borrowed queries");
    }

    /** GIVEN - a sweep-line over start/end events; the peak running count is the answer. */
    public static int maxConcurrentOverlap(List<QueryTiming> timings) {
        record Event(Instant at, int delta) {
        }
        List<Event> events = new ArrayList<>();
        for (QueryTiming timing : timings) {
            events.add(new Event(timing.start(), 1));
            events.add(new Event(timing.end(), -1));
        }
        events.sort(Comparator.comparing(Event::at));

        int current = 0;
        int max = 0;
        for (Event event : events) {
            current += event.delta();
            max = Math.max(max, current);
        }
        return max;
    }
}
