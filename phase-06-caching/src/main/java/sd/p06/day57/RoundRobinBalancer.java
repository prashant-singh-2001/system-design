package sd.p06.day57;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO(day57): the default everywhere, and the right first answer.
 *
 * <p>Hand out requests in strict rotation, skipping unhealthy backends. Use the
 * {@link AtomicInteger} counter and {@code Math.floorMod} - a plain {@code %} on an int that
 * eventually overflows to negative will throw, and it will do so months after deployment.
 *
 * <p>It assumes every request costs the same and every backend is equally fast. Both are usually
 * close enough to true, which is why round robin survives so well. Where it fails is long-lived
 * connections - WebSockets, gRPC streams - because rotating NEW connections evenly says nothing
 * about how many are still open.
 */
public final class RoundRobinBalancer implements LoadBalancer {

    private final List<Backend> backends;
    private final AtomicInteger counter = new AtomicInteger();

    public RoundRobinBalancer(List<Backend> backends) {
        this.backends = List.copyOf(backends);
    }

    @Override
    public Backend choose(String clientKey) {
        throw new UnsupportedOperationException("TODO(day57): rotate over the healthy backends");
    }

    @Override
    public String name() {
        return "round-robin";
    }
}
