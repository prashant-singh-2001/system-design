package sd.p06.day53;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sd.p06.support.MutableClock;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class Day53EvictionTest {

    private static final Duration TTL = Duration.ofMinutes(5);

    private MutableClock clock() {
        return MutableClock.startingAt("2026-03-01T12:00:00Z");
    }

    private InstrumentedCache cache(int capacity, EvictionPolicy policy, MutableClock clock) {
        return new InstrumentedCache(capacity, TTL, clock, policy);
    }

    @Test
    @DisplayName("hit ratio is hits over lookups")
    void hitRatio() {
        InstrumentedCache cache = cache(10, new LruEvictionPolicy(), clock());
        cache.put("a", "1");

        cache.get("a");
        cache.get("a");
        cache.get("a");
        cache.get("nope");

        assertThat(cache.stats().hits()).isEqualTo(3);
        assertThat(cache.stats().misses()).isEqualTo(1);
        assertThat(cache.stats().hitRatio()).isEqualTo(0.75, within(1e-9));
    }

    @Test
    @DisplayName("an empty cache reports 0% rather than dividing by zero")
    void emptyStats() {
        assertThat(cache(10, new LruEvictionPolicy(), clock()).stats().hitRatio()).isZero();
    }

    @Test
    @DisplayName("TTL: an entry past its deadline is an expiration and a miss")
    void ttlExpiry() {
        MutableClock clock = clock();
        InstrumentedCache cache = cache(10, new LruEvictionPolicy(), clock);
        cache.put("a", "1");

        clock.advance(Duration.ofMinutes(4));
        assertThat(cache.get("a")).as("still fresh").contains("1");

        clock.advance(Duration.ofMinutes(2));
        assertThat(cache.get("a")).as("six minutes old against a five minute TTL").isEmpty();

        assertThat(cache.stats().expirations()).isEqualTo(1);
        assertThat(cache.size()).as("an expired entry must not linger in memory").isZero();
    }

    @Test
    @DisplayName("capacity is never exceeded, and evictions are counted")
    void capacityIsRespected() {
        InstrumentedCache cache = cache(3, new LruEvictionPolicy(), clock());
        for (int i = 0; i < 10; i++) {
            cache.put("k" + i, "v" + i);
        }

        assertThat(cache.size()).isEqualTo(3);
        assertThat(cache.stats().evictions()).isEqualTo(7);
    }

    @Test
    @DisplayName("overwriting an existing key evicts nothing")
    void overwriteIsNotAnInsert() {
        InstrumentedCache cache = cache(3, new LruEvictionPolicy(), clock());
        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");

        cache.put("a", "updated");

        assertThat(cache.stats().evictions()).isZero();
        assertThat(cache.get("a")).contains("updated");
        assertThat(cache.get("b")).contains("2");
    }

    @Test
    @DisplayName("LRU evicts the least recently USED, not the oldest inserted")
    void lruOrdering() {
        InstrumentedCache cache = cache(3, new LruEvictionPolicy(), clock());
        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");

        cache.get("a");
        cache.put("d", "4");

        assertThat(cache.get("a")).as("recently accessed, so it survives").contains("1");
        assertThat(cache.get("b")).as("least recently used, so it goes").isEmpty();
        assertThat(cache.get("c")).contains("3");
        assertThat(cache.get("d")).contains("4");
    }

    @Test
    @DisplayName("LFU evicts the least frequently used, and resists a scan")
    void lfuOrdering() {
        InstrumentedCache cache = cache(3, new LfuEvictionPolicy(), clock());
        cache.put("hot", "1");
        cache.put("warm", "2");
        cache.put("cold", "3");

        for (int i = 0; i < 10; i++) {
            cache.get("hot");
        }
        cache.get("warm");

        cache.put("scan", "4");

        assertThat(cache.get("cold")).isEmpty();
        assertThat(cache.get("hot")).as("popularity protects it").contains("1");
        assertThat(cache.get("warm")).contains("2");
    }

    @Test
    @DisplayName("LFU ties break deterministically, so the behaviour is testable")
    void lfuTieBreak() {
        LfuEvictionPolicy policy = new LfuEvictionPolicy();
        policy.recordInsert("b");
        policy.recordInsert("a");
        policy.recordInsert("c");

        assertThat(policy.evictionCandidate())
                .as("all three have count 1, so the smallest key wins")
                .isEqualTo("a");
    }

    @Test
    @DisplayName("a policy tracking nothing has no candidate")
    void emptyPolicy() {
        assertThat(new LruEvictionPolicy().evictionCandidate()).isNull();
        assertThat(new LfuEvictionPolicy().evictionCandidate()).isNull();
    }

    @Test
    @DisplayName("a zero-capacity cache is not a cache")
    void rejectsNonsense() {
        assertThatThrownBy(() -> cache(0, new LruEvictionPolicy(), clock()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
