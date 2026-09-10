package sd.p06.day59;

import java.time.Duration;
import java.util.Optional;

/**
 * TODO(day59): parse and render a {@code Cache-Control} header.
 *
 * <p>This header is the largest cache you will ever operate. Every browser, every proxy and
 * every CDN edge obeys it, which means one correct header can remove more load than a whole
 * Redis tier - and one wrong one can serve a logged-in user's page to a stranger.
 *
 * <p>The directives that matter:
 * <ul>
 *   <li>{@code no-store} - never write this down anywhere. For genuinely sensitive responses.</li>
 *   <li>{@code no-cache} - store it, but revalidate before every reuse. Badly named: it does NOT
 *       mean "do not cache".</li>
 *   <li>{@code private} - browsers may cache it; SHARED caches (CDNs, proxies) must not. This is
 *       the directive standing between a per-user response and a CDN serving it to everyone.</li>
 *   <li>{@code public} - shared caches may store it.</li>
 *   <li>{@code max-age=N} - fresh for N seconds.</li>
 *   <li>{@code s-maxage=N} - like max-age but only for shared caches, and it OVERRIDES max-age
 *       there. Lets you say "browsers 60 seconds, CDN an hour".</li>
 *   <li>{@code must-revalidate} - once stale, never serve it without checking.</li>
 * </ul>
 *
 * <p>Parsing rules: comma-separated, trim each directive, case-insensitive, ignore anything you
 * do not recognise (that is how HTTP stays extensible). A malformed {@code max-age=abc} is
 * ignored rather than fatal - a bad header should not take a page down.
 *
 * <p>{@code format()} must emit directives in this fixed order, comma-space separated, so it is
 * testable: no-store, no-cache, private, public, max-age, s-maxage, must-revalidate. An empty
 * set of directives formats as an empty string.
 */
public record CacheControl(boolean noStore,
                           boolean noCache,
                           boolean isPrivate,
                           boolean isPublic,
                           Optional<Duration> maxAge,
                           Optional<Duration> sharedMaxAge,
                           boolean mustRevalidate) {

    public static CacheControl parse(String header) {
        throw new UnsupportedOperationException("TODO(day59): parse the directives");
    }

    public String format() {
        throw new UnsupportedOperationException("TODO(day59): render the directives in order");
    }

    /** A response no cache may store at all. Named to avoid clashing with the accessor. */
    public static CacheControl neverStore() {
        return new CacheControl(true, false, false, false,
                Optional.empty(), Optional.empty(), false);
    }

    /** The common public-asset case. */
    public static CacheControl publicFor(Duration maxAge) {
        return new CacheControl(false, false, false, true,
                Optional.of(maxAge), Optional.empty(), false);
    }
}
