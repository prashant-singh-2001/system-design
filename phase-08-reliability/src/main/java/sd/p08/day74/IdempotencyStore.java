package sd.p08.day74;

import java.time.Duration;
import java.util.Optional;

/**
 * Where the record of "I have already done this" lives.
 *
 * <p>In production this is Redis or a database table with a unique constraint - somewhere durable
 * and shared, because an in-memory map gives you idempotency per instance, which is no idempotency
 * at all behind a load balancer.
 */
public interface IdempotencyStore {

    /**
     * Atomically claim a key. Returns true if this caller won and should do the work; false if
     * somebody already has it.
     *
     * <p>Atomicity is the whole contract. A get-then-put has a window in which two concurrent
     * retries both see "absent" and both proceed - which is the exact bug idempotency exists to
     * prevent, reintroduced by a careless implementation.
     */
    boolean claim(String key, Duration ttl);

    Optional<PaymentResult> completedResult(String key);

    void storeResult(String key, PaymentResult result, Duration ttl);

    void release(String key);
}
