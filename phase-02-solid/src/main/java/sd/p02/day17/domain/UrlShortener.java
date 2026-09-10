package sd.p02.day17.domain;

import java.time.Clock;
import java.util.Optional;

/**
 * TODO(day17): the domain service - the hexagon itself.
 *
 * <p>Constructor takes {@link LinkRepository}, {@link CodeGenerator} and a
 * {@link java.time.Clock}. All three are injected. The clock matters as much as the other
 * two: {@code Instant.now()} inside a domain object makes time untestable, and time is a
 * dependency exactly like a database is.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code shorten(targetUrl)} - reject anything not starting {@code http://} or
 *       {@code https://} with {@code IllegalArgumentException}. If this URL was already
 *       shortened, return the EXISTING link rather than minting a second code. Otherwise
 *       generate a code, save, and return the new link.</li>
 *   <li>{@code resolve(code)} - the target URL, or empty.</li>
 * </ul>
 *
 * <p>That idempotency rule is a business decision, and notice where it lives: here, in the
 * domain, not in a repository and not in a controller. Business rules belong inside the
 * hexagon. Everything outside it is a detail about how the world reaches them.
 *
 * <p>The constraint for today: this file, and every file in this package, may import NOTHING
 * from {@code sd.p02.day17.adapter}. The test enforces it.
 */
public final class UrlShortener {

    public UrlShortener(LinkRepository repository, CodeGenerator codeGenerator, Clock clock) {
        throw new UnsupportedOperationException("TODO(day17): store the injected ports");
    }

    public ShortLink shorten(String targetUrl) {
        throw new UnsupportedOperationException("TODO(day17): validate, dedupe, generate, save");
    }

    public Optional<String> resolve(String code) {
        throw new UnsupportedOperationException("TODO(day17): look the code up");
    }
}
