package sd.p03.day21;

/**
 * TODO(day21): the FACTORY. Strategy gives you interchangeable implementations; Factory gives
 * you a single place that turns a runtime choice - a config value, an environment variable, a
 * feature flag - into the right one, so nothing else in the codebase says {@code new
 * Fnv1aHashFunction()} directly.
 *
 * <p>{@code byName("java")} -&gt; {@link JavaHashFunction}; {@code byName("fnv1a")} -&gt;
 * {@link Fnv1aHashFunction}; anything else -&gt; {@code IllegalArgumentException("unknown hash
 * function: " + name)}.
 *
 * <p>That last case matters as much as the two real ones: a factory that silently falls back to
 * a default for an unrecognised name turns a typo in a config file into a production incident
 * that only shows up as "why did every key move".
 */
public final class HashFunctions {

    private HashFunctions() {
    }

    public static HashFunction byName(String name) {
        throw new UnsupportedOperationException("TODO(day21): dispatch on name, or throw");
    }
}
