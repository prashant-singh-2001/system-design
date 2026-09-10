package sd.p06.day57;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * TODO(day57): route by a hash of the client key, so the same client lands on the same backend.
 *
 * <p>Sort the healthy backends by id (so the mapping does not depend on list order), then index
 * with {@code Math.floorMod(hash(clientKey), healthy.size())}.
 *
 * <p>What it buys: locality. A given user's session, or a given cache key, consistently reaches
 * one backend, so that backend's local cache actually gets warm. This is the mechanism behind
 * nginx's {@code hash $remote_addr} and behind routing cache keys to cache shards.
 *
 * <p>What it costs, and this is the important part: **it is modulo hashing again**, so it has
 * exactly the Day 49 problem. One backend going unhealthy changes the divisor and remaps almost
 * every client. For sticky sessions that logs everybody out; for a cache tier it cold-starts the
 * lot. The production answer is to hash onto a consistent hash RING instead - which is what you
 * built yesterday, and why yesterday came first.
 */
public final class HashBalancer implements LoadBalancer {

    private final List<Backend> backends;

    public HashBalancer(List<Backend> backends) {
        this.backends = List.copyOf(backends);
    }

    @Override
    public Backend choose(String clientKey) {
        throw new UnsupportedOperationException("TODO(day57): hash the client key onto a backend");
    }

    /** GIVEN - FNV-1a again, kept positive. */
    static int hash(String value) {
        long hash = 2166136261L;
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            hash ^= (b & 0xffL);
            hash *= 16777619L;
        }
        return (int) (hash & 0x7fffffffL);
    }

    @Override
    public String name() {
        return "hash";
    }
}
