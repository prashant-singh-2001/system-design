package sd.p08.day77;

import java.time.Instant;

/**
 * A lock held until an expiry, carrying a fencing token.
 *
 * @param token a monotonically increasing number issued with every successful acquisition. This
 *              is the field that makes distributed locking actually safe - see
 *              {@link FencedResource}.
 */
public record Lease(String resource, String owner, long token, Instant expiresAt) {
}
