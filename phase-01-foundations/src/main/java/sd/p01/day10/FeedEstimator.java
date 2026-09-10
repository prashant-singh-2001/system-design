package sd.p01.day10;

/**
 * Day 10 - the phase review. Everything from days 1-9 pointed at one question:
 * <em>can a feed be built with fanout-on-write?</em>
 *
 * <p>You already have the read and write QPS from Day 2. The numbers below are the ones that
 * decide the architecture. Compute them and the answer stops being a matter of opinion.
 */
public final class FeedEstimator {

    private FeedEstimator() {
    }

    /**
     * TODO(day10): fanout-on-write means every post is copied into every follower's feed.
     * So the real write load is {@code postsPerSecond x averageFollowers}.
     *
     * <p>Run it for 4,000 posts/s and 200 average followers before you read on. The number
     * you get is why this is the pivotal calculation in feed design, and why real systems
     * use a hybrid: fanout-on-write for ordinary accounts, fanout-on-read for the handful
     * of accounts with millions of followers.
     */
    public static long fanoutWritesPerSecond(long postsPerSecond, long averageFollowers) {
        throw new UnsupportedOperationException("TODO(day10): implement fanoutWritesPerSecond");
    }

    /**
     * TODO(day10): the 80/20 rule. Roughly 20% of the data serves 80% of requests, so you
     * size the cache for the hot fraction rather than the whole dataset.
     *
     * <p>Return {@code totalItems x bytesPerItem x hotFraction}.
     */
    public static long hotDatasetBytes(long totalItems, int bytesPerItem, double hotFraction) {
        throw new UnsupportedOperationException("TODO(day10): implement hotDatasetBytes");
    }

    /**
     * TODO(day10): how many machines to hold that hot set, rounding UP.
     *
     * <p>Rounding up matters: 2.1 servers is 3 servers. Interviewers notice when you round
     * the wrong way, because it means you are treating the arithmetic as a formality rather
     * than as a capacity decision.
     */
    public static int serversNeeded(long totalBytes, long bytesPerServer) {
        throw new UnsupportedOperationException("TODO(day10): implement serversNeeded");
    }

    /** TODO(day10): the same rounding-up logic applied to request throughput. */
    public static int appServersNeeded(long peakQps, long qpsPerServer) {
        throw new UnsupportedOperationException("TODO(day10): implement appServersNeeded");
    }

    /**
     * TODO(day10): the hybrid decision rule.
     *
     * <p>Return {@code true} when an account should be handled by fanout-on-READ - that is,
     * when its follower count exceeds {@code threshold}. Fanning out one celebrity post to
     * 50 million inboxes is a write storm; instead you leave it in place and merge it in at
     * read time.
     *
     * <p>This one-line method is the answer to the most common follow-up question in a feed
     * design interview.
     */
    public static boolean useFanoutOnRead(long followerCount, long threshold) {
        throw new UnsupportedOperationException("TODO(day10): implement useFanoutOnRead");
    }
}
