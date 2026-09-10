package sd.p03.day28;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * GIVEN - a stand-in for something genuinely expensive to create, like a database connection.
 * Every instance records its own creation order, so a test can prove the pool is REUSING
 * instances rather than quietly creating a fresh one per borrow.
 */
public final class PooledResource {

    private static final AtomicInteger CREATED_COUNT = new AtomicInteger(0);

    private final int id;

    public PooledResource() {
        this.id = CREATED_COUNT.incrementAndGet();
    }

    public int id() {
        return id;
    }

    public static int createdCount() {
        return CREATED_COUNT.get();
    }
}
