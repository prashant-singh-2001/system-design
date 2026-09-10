package sd.p03.day24;

import java.util.function.Supplier;

/**
 * TODO(day24): a PROXY used for the OTHER classic reason - not adding behaviour, but controlling
 * WHEN the real object gets built. If constructing the real {@link SlowService} is itself
 * expensive (opening a connection pool, warming a cache), you do not want to pay for it until
 * something actually calls {@code fetch}.
 *
 * <p>Call {@code factory.get()} at most once, the first time {@code fetch} is invoked, and reuse
 * the same instance for every call after that. This version does not need to be thread-safe -
 * Day 28 covers the correct, thread-safe version of exactly this problem (a lazily-initialised
 * singleton), and the contrast is worth noticing once you get there.
 */
public final class LazyServiceProxy implements SlowService {

    public LazyServiceProxy(Supplier<SlowService> factory) {
        throw new UnsupportedOperationException("TODO(day24): store the factory, nothing else yet");
    }

    @Override
    public String fetch(String key) {
        throw new UnsupportedOperationException(
                "TODO(day24): build the real service on first use only, then delegate");
    }
}
