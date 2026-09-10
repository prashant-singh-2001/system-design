package sd.p06.day57;

import java.util.concurrent.atomic.AtomicInteger;

/** GIVEN - one server behind the balancer. */
public final class Backend {

    private final String id;
    private final int weight;
    private final AtomicInteger activeConnections = new AtomicInteger();
    private volatile boolean healthy = true;

    public Backend(String id) {
        this(id, 1);
    }

    public Backend(String id, int weight) {
        if (weight < 1) {
            throw new IllegalArgumentException("weight must be at least 1");
        }
        this.id = id;
        this.weight = weight;
    }

    public String id() {
        return id;
    }

    public int weight() {
        return weight;
    }

    public boolean healthy() {
        return healthy;
    }

    public void markUnhealthy() {
        healthy = false;
    }

    public void markHealthy() {
        healthy = true;
    }

    public int activeConnections() {
        return activeConnections.get();
    }

    public void openConnection() {
        activeConnections.incrementAndGet();
    }

    public void closeConnection() {
        activeConnections.decrementAndGet();
    }

    @Override
    public String toString() {
        return id;
    }
}
