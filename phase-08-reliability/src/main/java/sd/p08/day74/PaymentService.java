package sd.p08.day74;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO(day74): idempotency keys - the pattern that makes at-least-once safe.
 *
 * <p>Phase 7 kept borrowing this. Here is the actual mechanism.
 *
 * <p>The problem: a client sends "charge this card", the network times out, and the client has no
 * way to tell whether the charge happened. Both possible actions are wrong - retrying may
 * double-charge, not retrying may lose the payment. The uncertainty is irreducible; you cannot
 * fix it with a better network.
 *
 * <p>So you move the decision to the server. The client generates a unique key per logical
 * operation and sends it with every attempt. The server does the work at most once per key and
 * returns the same answer to every repeat. Retrying becomes safe, so the client's choice is easy.
 *
 * <p>Three properties that are easy to get wrong:
 * <ul>
 *   <li><b>The client generates the key, not the server.</b> A server-generated key changes on
 *       each retry and deduplicates nothing.</li>
 *   <li><b>The key covers a logical operation, not a request.</b> Same key for the retry of the
 *       same intent; a new key for a genuinely new payment. "Charge Alice £50" twice on purpose is
 *       two operations and needs two keys.</li>
 *   <li><b>Return the ORIGINAL result on replay, not just a success.</b> The client needs the
 *       same payment id it would have received first time.</li>
 * </ul>
 *
 * <p>Implement {@code charge}:
 * <ol>
 *   <li>If a completed result exists for the key, return it marked as a replay - without touching
 *       the payment gateway.</li>
 *   <li>Otherwise try to {@code claim} the key. If the claim fails, another attempt is in flight
 *       concurrently: throw {@link ConcurrentAttemptException} so the client retries shortly.</li>
 *   <li>Having won the claim, do the work, store the result, and return it.</li>
 *   <li>If the work throws, {@code release} the claim and rethrow - a failed attempt must not
 *       block the retry forever, or a transient failure becomes permanent.</li>
 * </ol>
 *
 * <p>The trade-off: you now operate a store, its TTL bounds how long retries are safe, and a key
 * reused for a different payload is a genuine correctness hazard (real APIs return 422 for that).
 * In exchange, every client can retry freely - which is what makes the rest of Phase 8 workable.
 */
public final class PaymentService {

    /** Thrown when another attempt with the same key is currently in flight. */
    public static final class ConcurrentAttemptException extends RuntimeException {
        public ConcurrentAttemptException(String message) {
            super(message);
        }
    }

    private static final Duration TTL = Duration.ofHours(24);

    private final IdempotencyStore store;
    private final AtomicInteger gatewayCalls = new AtomicInteger();
    private final AtomicInteger paymentIdSequence = new AtomicInteger();

    public PaymentService(IdempotencyStore store) {
        this.store = store;
    }

    public PaymentResult charge(String idempotencyKey, String orderId, long amountCents) {
        throw new UnsupportedOperationException("TODO(day74): replay, claim, work, store, release");
    }

    /**
     * TODO(day74): the actual side effect. Increment {@code gatewayCalls}, mint a payment id of
     * the form {@code "pay-" + sequence}, and return the result. Throw
     * {@code IllegalStateException("gateway declined")} when {@code amountCents} is not positive,
     * so the test can exercise the failure path.
     */
    private PaymentResult callGateway(String orderId, long amountCents) {
        throw new UnsupportedOperationException("TODO(day74): the side effect we must not repeat");
    }

    /** How many times the payment gateway was actually called. The number that matters. */
    public int gatewayCalls() {
        return gatewayCalls.get();
    }
}
