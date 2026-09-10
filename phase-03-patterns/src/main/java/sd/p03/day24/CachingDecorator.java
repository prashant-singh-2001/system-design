package sd.p03.day24;

/**
 * TODO(day24): another DECORATOR, structurally identical to {@link TimingDecorator} - which is
 * exactly the point. Decorator is a SHAPE (wrap the same interface, delegate, add one thing),
 * not a single fixed behaviour. This one happens to also be a textbook Proxy: from the caller's
 * side it stands in for the real service and transparently serves some requests without ever
 * reaching it - "caching proxy" and "caching decorator" are the same object described from two
 * different angles.
 *
 * <p>On a cache hit, return the stored value and do not call the delegate at all. On a miss,
 * call the delegate, store ONLY a successful result, and return it. Do not cache a thrown
 * exception - a transient failure should get to succeed and be cached the next time it is
 * retried, not be permanently remembered as a failure.
 */
public final class CachingDecorator implements SlowService {

    public CachingDecorator(SlowService delegate) {
        throw new UnsupportedOperationException("TODO(day24): store the delegate, init a cache");
    }

    @Override
    public String fetch(String key) {
        throw new UnsupportedOperationException(
                "TODO(day24): serve from cache, or call through and cache on success");
    }

    /** How many distinct keys are currently cached - useful for a test to inspect. */
    public int size() {
        throw new UnsupportedOperationException("TODO(day24): implement size");
    }
}
