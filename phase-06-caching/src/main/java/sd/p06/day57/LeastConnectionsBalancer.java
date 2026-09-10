package sd.p06.day57;

import java.util.List;

/**
 * TODO(day57): send the request to whoever is least busy right now.
 *
 * <p>Pick the healthy backend with the fewest active connections. Break ties by id, ascending,
 * so the behaviour is deterministic and testable.
 *
 * <p>This is the strategy that actually adapts. Round robin is blind: if backend B is stuck on
 * slow requests, round robin keeps feeding it at exactly the same rate as everyone else. Least
 * connections notices, because a slow backend accumulates open connections, and steers traffic
 * away without anybody configuring anything.
 *
 * <p>That makes it the right default for long-lived connections and for workloads where request
 * cost varies a lot. Its cost is that the balancer must now track per-backend state, which is
 * fine in one process and genuinely hard across a fleet of balancers that each see only their
 * own share of the traffic.
 */
public final class LeastConnectionsBalancer implements LoadBalancer {

    private final List<Backend> backends;

    public LeastConnectionsBalancer(List<Backend> backends) {
        this.backends = List.copyOf(backends);
    }

    @Override
    public Backend choose(String clientKey) {
        throw new UnsupportedOperationException("TODO(day57): fewest active connections, ties by id");
    }

    @Override
    public String name() {
        return "least-connections";
    }
}
