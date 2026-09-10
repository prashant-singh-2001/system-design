package sd.p03.day28;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * GIVEN - the FLYWEIGHT itself: small, immutable, and safe to share one instance of across
 * every order, shipment or address that happens to reference the same country. Multiply the
 * per-instance overhead by a billion order rows referencing "US" and the saving stops being
 * theoretical.
 *
 * <p>{@code createdCount()} is test instrumentation only - it lets {@code
 * Day28SingletonPoolFlyweightTest} prove {@link CountryFactory} is not silently constructing a
 * fresh {@code Country} on every lookup.
 */
public record Country(String isoCode, String name) {

    private static final AtomicInteger CREATED_COUNT = new AtomicInteger(0);

    public Country {
        CREATED_COUNT.incrementAndGet();
    }

    public static int createdCount() {
        return CREATED_COUNT.get();
    }
}
