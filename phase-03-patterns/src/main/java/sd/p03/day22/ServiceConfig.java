package sd.p03.day22;

import java.util.Map;

/**
 * TODO(day22): an immutable config, built by a fluent {@link Builder} rather than a telescoping
 * constructor.
 *
 * <p>Two separate concerns live in the builder, and it is worth keeping them separate in your
 * head:
 * <ul>
 *   <li><b>Per-field validation</b>, checked the moment a setter is called - {@code port} out
 *       of range, a negative {@code timeoutMs} - because there is no reason to wait for
 *       {@code build()} to reject a value that is wrong in isolation.</li>
 *   <li><b>Cross-field / completeness validation</b>, checked only in {@code build()} - a
 *       missing {@code host} cannot be caught by any single setter, because "missing" is a
     *       property of the whole object, not of one field.</li>
 * </ul>
 *
 * <p>Fields and rules:
 * <ul>
 *   <li>{@code host} - required, non-blank. Missing at {@code build()} -&gt;
 *       {@code IllegalStateException("host is required")}.</li>
 *   <li>{@code port} - 1 to 65535 inclusive, checked in the setter -&gt;
 *       {@code IllegalArgumentException} if out of range.</li>
 *   <li>{@code timeoutMs} - must be positive, checked in the setter.</li>
 *   <li>{@code maxConnections} - at least 1, checked in the setter.</li>
 *   <li>{@code retries} - non-negative, checked in the setter. Defaults to 0.</li>
 *   <li>{@code tls(certPath)} - sets {@code tlsEnabled = true} and stores the path. Absent by
 *       default ({@code tlsEnabled() == false}, {@code certPath() == null}).</li>
 *   <li>{@code tag(key, value)} - accumulates into an unmodifiable map, copied defensively at
 *       {@code build()} so later mutation of the builder can never leak into an already-built
 *       config (Day 16 covers why this copy has to happen on both doors).</li>
 * </ul>
 *
 * <p>Finally, the PROTOTYPE half of today: {@link #toBuilder()} returns a {@code Builder}
 * pre-populated from this instance's current values, so a caller can clone-and-modify -
 * {@code "give me a copy of prod with retries turned up"} - instead of re-specifying every
 * field from scratch.
 */
public final class ServiceConfig {

    public static Builder builder() {
        throw new UnsupportedOperationException("TODO(day22): return a fresh Builder");
    }

    public String host() {
        throw new UnsupportedOperationException("TODO(day22)");
    }

    public int port() {
        throw new UnsupportedOperationException("TODO(day22)");
    }

    public long timeoutMs() {
        throw new UnsupportedOperationException("TODO(day22)");
    }

    public int maxConnections() {
        throw new UnsupportedOperationException("TODO(day22)");
    }

    public int retries() {
        throw new UnsupportedOperationException("TODO(day22)");
    }

    public boolean tlsEnabled() {
        throw new UnsupportedOperationException("TODO(day22)");
    }

    public String certPath() {
        throw new UnsupportedOperationException("TODO(day22)");
    }

    public Map<String, String> tags() {
        throw new UnsupportedOperationException("TODO(day22): return an unmodifiable view");
    }

    /** PROTOTYPE: clone this config into a builder seeded with its current values. */
    public Builder toBuilder() {
        throw new UnsupportedOperationException("TODO(day22): seed a new Builder from `this`");
    }

    public static final class Builder {

        public Builder host(String host) {
            throw new UnsupportedOperationException("TODO(day22): store it");
        }

        public Builder port(int port) {
            throw new UnsupportedOperationException("TODO(day22): validate the range, then store");
        }

        public Builder timeoutMs(long timeoutMs) {
            throw new UnsupportedOperationException("TODO(day22): validate positive, then store");
        }

        public Builder maxConnections(int maxConnections) {
            throw new UnsupportedOperationException("TODO(day22): validate >= 1, then store");
        }

        public Builder retries(int retries) {
            throw new UnsupportedOperationException("TODO(day22): validate >= 0, then store");
        }

        public Builder tls(String certPath) {
            throw new UnsupportedOperationException("TODO(day22): enable tls, store the path");
        }

        public Builder tag(String key, String value) {
            throw new UnsupportedOperationException("TODO(day22): accumulate into the tag map");
        }

        public ServiceConfig build() {
            throw new UnsupportedOperationException(
                    "TODO(day22): check completeness, defensively copy tags, construct");
        }
    }
}
