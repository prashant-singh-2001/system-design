package sd.p03.day28;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * TODO(day28): a BOUNDED pool of reusable, expensive-to-create objects - the shape behind a real
 * JDBC connection pool, minus the SQL.
 *
 * <p>Two collaborators do all the work:
 * <ul>
 *   <li>a {@link Semaphore} initialised with {@code maxSize} permits - it is the thing that
 *       makes {@link #borrow()} BLOCK once {@code maxSize} items are already checked out,
 *       rather than letting the pool grow without limit (Day 5 and Day 6 already covered why an
     *       unbounded resource under load is an outage, not a convenience)</li>
 *   <li>a queue of currently-idle instances - {@link ConcurrentLinkedQueue} is a reasonable
 *       choice</li>
 * </ul>
 *
 * <p>{@code borrow()}: acquire a permit (blocking if none are free), then poll the idle queue -
 * if it has an instance, return that one; if it is empty, create a new one via {@code factory}.
 * Either way, the pool now has exactly one more item checked out than a moment ago.
 *
 * <p>{@code release(item)}: put {@code item} back on the idle queue, then release a permit. Do
 * these in that ORDER - releasing the permit first could let another thread's {@code borrow()}
 * find an empty idle queue and construct a brand-new instance instead of reusing the one you
 * were about to return, which defeats the entire point of pooling under load.
 */
public final class ObjectPool<T> {

    public ObjectPool(Supplier<T> factory, int maxSize) {
        throw new UnsupportedOperationException("TODO(day28): store the factory, size the semaphore");
    }

    public T borrow() {
        throw new UnsupportedOperationException(
                "TODO(day28): acquire a permit, then reuse an idle instance or create one");
    }

    public void release(T item) {
        throw new UnsupportedOperationException(
                "TODO(day28): return the item to the idle queue, THEN release the permit");
    }
}
