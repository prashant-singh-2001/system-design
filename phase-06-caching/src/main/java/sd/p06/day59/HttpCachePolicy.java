package sd.p06.day59;

import java.time.Duration;
import java.time.Instant;

/**
 * TODO(day59): decide what a cache may do with a stored response.
 *
 * <p>{@code isStorable} - may this response be written down at all?
 * {@code no-store} means no, for anyone. {@code private} means a SHARED cache must refuse while
 * a browser may keep it. Getting this one wrong is the "CDN served my account page to another
 * user" incident, so treat it as a correctness rule rather than a performance one.
 *
 * <p>{@code evaluate} - given a stored response's age, what now?
 * <ol>
 *   <li>Not storable -> {@code NOT_USABLE}.</li>
 *   <li>{@code no-cache} -> {@code MUST_REVALIDATE}, however young it is.</li>
 *   <li>Work out the lifetime: a shared cache uses {@code s-maxage} when present, otherwise
 *       {@code max-age}; a private cache always uses {@code max-age}. No lifetime at all means
 *       {@code MUST_REVALIDATE} - absent freshness information is not permission.</li>
 *   <li>Age below the lifetime -> {@code FRESH}. At or beyond it, {@code MUST_REVALIDATE},
 *       or {@code NOT_USABLE} when {@code must-revalidate} is set.</li>
 * </ol>
 *
 * <p>{@code isNotModified} - the revalidation itself. The client sends
 * {@code If-None-Match: <etag>}; if it matches the current ETag, the origin answers 304 with no
 * body. You still pay a round trip, but not the bytes - which for a large asset over a slow link
 * is most of the cost.
 */
public final class HttpCachePolicy {

    private HttpCachePolicy() {
    }

    public static boolean isStorable(CacheControl control, boolean sharedCache) {
        throw new UnsupportedOperationException("TODO(day59): no-store, and private in a shared cache");
    }

    public static Freshness evaluate(CacheControl control, Duration age, boolean sharedCache) {
        throw new UnsupportedOperationException("TODO(day59): storable, no-cache, lifetime, age");
    }

    /** GIVEN - age of a stored response. */
    public static Duration age(Instant storedAt, Instant now) {
        return Duration.between(storedAt, now);
    }

    public static boolean isNotModified(String ifNoneMatch, String currentEtag) {
        throw new UnsupportedOperationException("TODO(day59): compare the ETags");
    }
}
