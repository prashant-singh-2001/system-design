package sd.p06.day57;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO(day57): round robin for a fleet where the machines are not identical.
 *
 * <p>A backend with weight 3 should receive three times the traffic of one with weight 1. The
 * simplest correct approach: build an expansion list containing each healthy backend repeated
 * {@code weight} times, then rotate over that.
 *
 * <p>Rebuild the expansion on every call from the currently HEALTHY backends. Caching it is an
 * obvious optimisation and an equally obvious bug - a backend that dies keeps getting traffic
 * until something invalidates your cache.
 *
 * <p>This is how you handle a mixed fleet after a hardware refresh, and it is the same mechanism
 * as consistent hashing's virtual nodes: weight is just "how many slots do you own".
 */
public final class WeightedRoundRobinBalancer implements LoadBalancer {

    private final List<Backend> backends;
    private final AtomicInteger counter = new AtomicInteger();

    public WeightedRoundRobinBalancer(List<Backend> backends) {
        this.backends = List.copyOf(backends);
    }

    @Override
    public Backend choose(String clientKey) {
        throw new UnsupportedOperationException("TODO(day57): expand by weight, then rotate");
    }

    @Override
    public String name() {
        return "weighted-round-robin";
    }
}
