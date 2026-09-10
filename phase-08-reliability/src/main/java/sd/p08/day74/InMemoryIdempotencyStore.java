package sd.p08.day74;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO(day74): a correct-enough store for the exercise.
 *
 * <p>{@code claim} must be ATOMIC. Use {@code ConcurrentHashMap.putIfAbsent} - a
 * {@code containsKey} followed by a {@code put} has a race window wide enough for two retries to
 * both proceed, which is precisely the bug you are here to prevent.
 *
 * <p>{@code completedResult} returns a stored result if the work has finished;
 * {@code storeResult} records it; {@code release} removes the claim so a FAILED attempt can be
 * retried. That last one matters: a key claimed by an attempt that crashed must not block the
 * retry forever, or a transient failure becomes a permanent one.
 *
 * <p>TTLs are ignored here for simplicity. In production they are not optional - they are what
 * stops the store growing without bound, and they set how long a client may safely retry.
 */
public final class InMemoryIdempotencyStore implements IdempotencyStore {

    private final Map<String, Boolean> claims = new ConcurrentHashMap<>();
    private final Map<String, PaymentResult> results = new ConcurrentHashMap<>();

    @Override
    public boolean claim(String key, Duration ttl) {
        throw new UnsupportedOperationException("TODO(day74): atomic claim - putIfAbsent");
    }

    @Override
    public Optional<PaymentResult> completedResult(String key) {
        throw new UnsupportedOperationException("TODO(day74): return a stored result if present");
    }

    @Override
    public void storeResult(String key, PaymentResult result, Duration ttl) {
        throw new UnsupportedOperationException("TODO(day74): record the outcome");
    }

    @Override
    public void release(String key) {
        throw new UnsupportedOperationException("TODO(day74): drop the claim so a retry can proceed");
    }

    public int claimCount() {
        return claims.size();
    }
}
