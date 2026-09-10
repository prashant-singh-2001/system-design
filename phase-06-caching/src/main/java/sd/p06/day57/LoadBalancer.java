package sd.p06.day57;

/**
 * Choosing where a request goes.
 *
 * <p>Every implementation must skip unhealthy backends, and must throw
 * {@link IllegalStateException} when none are healthy. Returning null or silently picking a dead
 * backend converts an outage into a mystery.
 */
public interface LoadBalancer {

    /**
     * @param clientKey something stable about the caller - an IP, a session id, a user id.
     *                  Most strategies ignore it; hash-based ones do not.
     */
    Backend choose(String clientKey);

    String name();
}
