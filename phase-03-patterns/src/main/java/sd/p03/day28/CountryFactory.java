package sd.p03.day28;

import java.util.Map;

/**
 * TODO(day28): the FLYWEIGHT FACTORY - the piece that turns "many callers ask for the same
 * value" into "one shared instance, handed out repeatedly". A small, fixed lookup table of
 * country names is given below; do not invent your own list, the test checks against this one.
 *
 * <p>{@code get(isoCode)} must return the EXACT SAME {@link Country} instance on every call for
 * the same code - not an equal one, the same one, checkable with {@code ==}. Build the cache
 * once (a {@code static final Map<String, Country>} populated eagerly, or a lazily-filled one -
 * either satisfies the contract, since the input set is small and fixed here).
 *
 * <p>An unknown code should throw {@code IllegalArgumentException}, not silently return
 * {@code null} - a caller that mistypes a code deserves to find out immediately, not three
 * layers up when something does {@code country.name()} on a null.
 */
public final class CountryFactory {

    private static final Map<String, String> NAMES = Map.of(
            "US", "United States",
            "GB", "United Kingdom",
            "FR", "France",
            "DE", "Germany",
            "JP", "Japan");

    private CountryFactory() {
    }

    public static Country get(String isoCode) {
        throw new UnsupportedOperationException(
                "TODO(day28): return the cached Country for isoCode, creating it once if absent");
    }
}
