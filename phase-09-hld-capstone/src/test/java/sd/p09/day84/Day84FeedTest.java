package sd.p09.day84;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sd.p09.day84.FeedPlanner.Post;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Day84FeedTest {

    private static Post post(String author, long time) {
        return new Post(author, author + "@" + time, time);
    }

    @Test
    @DisplayName("the pivotal number: 4,000 posts/s x 200 followers = 800,000 feed writes/s")
    void fanoutLoad() {
        assertThat(FeedPlanner.fanoutWritesPerSecond(4_000, 200)).isEqualTo(800_000L);
    }

    @Test
    @DisplayName("celebrities are handled on read; everyone else on write")
    void hybridRule() {
        assertThat(FeedPlanner.strategyFor(150, 100_000)).isEqualTo(FanoutStrategy.ON_WRITE);
        assertThat(FeedPlanner.strategyFor(50_000_000L, 100_000))
                .as("fanning one post out to 50 million inboxes is a write storm")
                .isEqualTo(FanoutStrategy.ON_READ);
    }

    @Test
    @DisplayName("THE argument for the hybrid, with a number attached")
    void hybridCollapsesTheWriteLoad() {
        // A realistic skew: mostly ordinary accounts, a handful of enormous ones.
        List<Long> followers = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            followers.add(200L);
        }
        followers.add(50_000_000L);
        followers.add(30_000_000L);

        double postsEach = 0.1;

        long pureFanout = followers.stream()
                .mapToLong(count -> Math.round(count * postsEach))
                .sum();
        long hybrid = FeedPlanner.hybridWritesPerSecond(followers, postsEach, 100_000);

        System.out.printf("%n  pure fanout-on-write : %,d writes/second%n", pureFanout);
        System.out.printf("  hybrid               : %,d writes/second%n", hybrid);
        System.out.printf("  reduction            : %.0fx%n%n", (double) pureFanout / hybrid);

        assertThat(hybrid)
                .as("""
                        Two accounts out of ten thousand produced almost all the write load.
                        Excluding them collapses it by orders of magnitude - and costs you a
                        second code path, forever. That is the trade, stated with a number.""")
                .isLessThan(pureFanout / 100);
    }

    @Test
    @DisplayName("the read path: merging sorted feeds, newest first")
    void mergeFeeds() {
        List<Post> alice = List.of(post("alice", 100), post("alice", 70), post("alice", 40));
        List<Post> bob = List.of(post("bob", 90), post("bob", 50));
        List<Post> carol = List.of(post("carol", 80), post("carol", 60));

        List<Post> feed = FeedPlanner.mergeFeed(List.of(alice, bob, carol), 5);

        assertThat(feed).extracting(Post::timestampMillis)
                .containsExactly(100L, 90L, 80L, 70L, 60L);
    }

    @Test
    @DisplayName("the limit is respected, and short inputs are fine")
    void mergeEdgeCases() {
        assertThat(FeedPlanner.mergeFeed(List.of(), 10)).isEmpty();
        assertThat(FeedPlanner.mergeFeed(List.of(List.of()), 10)).isEmpty();
        assertThat(FeedPlanner.mergeFeed(
                List.of(List.of(post("a", 10), post("a", 5))), 1)).hasSize(1);
    }

    @Test
    @DisplayName("merging many feeds is what makes fanout-on-read expensive")
    void mergeCostGrowsWithFollowing() {
        List<List<Post>> manyFeeds = new ArrayList<>();
        for (int author = 0; author < 500; author++) {
            List<Post> posts = new ArrayList<>();
            for (int i = 10; i > 0; i--) {
                posts.add(post("author-" + author, author * 100L + i));
            }
            manyFeeds.add(posts);
        }

        List<Post> feed = FeedPlanner.mergeFeed(manyFeeds, 50);

        assertThat(feed).hasSize(50);
        assertThat(feed.get(0).timestampMillis())
                .as("""
                        Following 500 accounts means touching 500 sources for one page of feed.
                        That is the read cost fanout-on-write exists to avoid - and why the hybrid
                        pays it only for the handful of celebrity accounts.""")
                .isGreaterThan(feed.get(49).timestampMillis());
    }
}
