package sd.p03.day29;

import java.util.List;
import java.util.function.Function;

/**
 * TODO(day29): a fixed pool of worker threads pulling from one bounded shared queue - the
 * PRODUCER-CONSUMER pattern, with a BOUNDED BUFFER for backpressure and POISON PILLS for a clean
 * shutdown. This is the shape underneath every thread-pool executor you have ever configured.
 *
 * <p>Build it around a {@code BlockingQueue<WorkItem>} (an {@code ArrayBlockingQueue} sized to
 * {@code queueCapacity} is a fine choice) and {@code workerCount} threads, each running the same
 * loop:
 *
 * <pre>
 *   while (true) {
 *       WorkItem item = queue.take();               // blocks until something arrives
 *       if (item instanceof PoisonPill) break;       // the shutdown signal - exit the loop
 *       Job job = (Job) item;
 *       try {
 *           int value = processor.apply(job);
 *           results.add(Result.success(job.id(), value));
 *       } catch (RuntimeException e) {
 *           results.add(Result.failure(job.id(), e.getMessage()));   // one bad job, not a dead worker
 *       }
 *   }
 * </pre>
 *
 * <p>{@code submit(job)}: {@code queue.put(job)} - which BLOCKS once the queue is at
 * {@code queueCapacity}, giving you backpressure for free, since a slow consumer now
 * automatically slows down a fast producer instead of the queue growing without limit. Reject a
 * submission after {@link #shutdown()} has been called with {@code IllegalStateException}.
 *
 * <p>{@code shutdown()}: mark the pool as no-longer-accepting-submissions, then {@code
 * queue.put(new PoisonPill())} once PER WORKER - exactly one pill per worker guarantees every
 * worker sees exactly one and exits, and because pills go to the BACK of the queue, every
 * already-submitted job is drained and processed first. Then join every worker thread, so
 * {@code shutdown()} does not return until every thread has actually exited - no leaked threads.
 *
 * <p>{@code queue.put} and {@code Thread.join} both declare {@code InterruptedException}. For
 * today, catch it, call {@code Thread.currentThread().interrupt()} to preserve the interrupt
 * status, and wrap it in an unchecked exception - propagating it silently swallowed would hide a
 * real shutdown signal from whoever is running this thread.
 */
public final class WorkerPool {

    public WorkerPool(int workerCount, int queueCapacity, Function<Job, Integer> processor) {
        throw new UnsupportedOperationException(
                "TODO(day29): create the bounded queue, start workerCount worker threads");
    }

    public void submit(Job job) {
        throw new UnsupportedOperationException(
                "TODO(day29): reject if shut down, otherwise queue.put(job)");
    }

    public void shutdown() {
        throw new UnsupportedOperationException(
                "TODO(day29): one poison pill per worker, then join every worker thread");
    }

    /** A snapshot of every result recorded so far. Safe to call before or after shutdown. */
    public List<Result> results() {
        throw new UnsupportedOperationException("TODO(day29): return a thread-safe snapshot");
    }

    /** How many worker threads are currently alive - zero once {@link #shutdown()} has returned. */
    public int liveWorkerCount() {
        throw new UnsupportedOperationException("TODO(day29): count workers where isAlive() is true");
    }
}
