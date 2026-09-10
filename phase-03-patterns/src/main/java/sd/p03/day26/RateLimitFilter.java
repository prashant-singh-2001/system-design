package sd.p03.day26;

/**
 * TODO(day26): the second link. A miniature preview of Day 33's token bucket, scaled down to a
 * flat per-IP counter with no time window at all - enough to teach the CHAIN shape today without
 * building the real algorithm early.
 *
 * <p>Track how many requests each {@code clientIp} has made THROUGH THIS FILTER INSTANCE's
 * lifetime. If a client is already at {@code maxRequestsPerClient}, return
 * {@code new Response(429, "rate limited")} without calling {@code next} and without
 * incrementing further. Otherwise increment that client's count and call {@code next}.
 */
public final class RateLimitFilter implements Filter {

    public RateLimitFilter(int maxRequestsPerClient) {
        throw new UnsupportedOperationException("TODO(day26): store the limit, init a counter map");
    }

    @Override
    public Response apply(Request request, Handler next) {
        throw new UnsupportedOperationException(
                "TODO(day26): 429 once a client hits the limit, otherwise count and delegate");
    }
}
