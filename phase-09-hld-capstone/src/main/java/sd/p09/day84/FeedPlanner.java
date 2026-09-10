package sd.p09.day84;

import java.util.List;

/**
 * TODO(day84): the calculation that decides how a feed is built.
 *
 * <p>You met the headline number on Day 10. Today it becomes a design.
 *
 * <p><b>Fanout-on-write</b> copies each post into every follower's precomputed inbox. Reads become
 * a single lookup - wonderful in a 50:1 read-heavy system. But the write load is
 * {@code posts/second x average followers}, and a 50-million-follower account turns one post into
 * 50 million writes.
 *
 * <p><b>Fanout-on-read</b> leaves posts in place and merges at read time. Writes are trivial;
 * reads must gather from everyone you follow and merge.
 *
 * <p>Every real system uses a <b>hybrid</b>, because the follower-count distribution is so skewed
 * that no single mechanism serves both ends. That is the answer, and the interesting part is being
 * able to defend the threshold with arithmetic rather than folklore.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code fanoutWritesPerSecond} - posts x average followers.</li>
 *   <li>{@code strategyFor(followerCount, threshold)} - above the threshold, fanout on read.</li>
 *   <li>{@code hybridWritesPerSecond} - given per-account follower counts and a posting rate each,
 *       total the write load counting ONLY the accounts below the threshold. This is the number
 *       the hybrid actually buys you.</li>
 *   <li>{@code mergeFeed} - merge already-sorted-descending post lists into one feed of at most
 *       {@code limit} entries, newest first. This is the read-path cost of fanout-on-read, and
 *       writing it makes that cost concrete.</li>
 * </ul>
 */
public final class FeedPlanner {

    private FeedPlanner() {
    }

    /** A post, with a timestamp to merge on. */
    public record Post(String authorId, String content, long timestampMillis) {
    }

    public static long fanoutWritesPerSecond(long postsPerSecond, long averageFollowers) {
        throw new UnsupportedOperationException("TODO(day84): posts x followers");
    }

    public static FanoutStrategy strategyFor(long followerCount, long threshold) {
        throw new UnsupportedOperationException("TODO(day84): celebrities fan out on read");
    }

    public static long hybridWritesPerSecond(List<Long> followerCounts, double postsPerAccountPerSecond,
                                             long threshold) {
        throw new UnsupportedOperationException("TODO(day84): only the accounts below the threshold");
    }

    public static List<Post> mergeFeed(List<List<Post>> sortedFeeds, int limit) {
        throw new UnsupportedOperationException("TODO(day84): k-way merge, newest first");
    }
}
