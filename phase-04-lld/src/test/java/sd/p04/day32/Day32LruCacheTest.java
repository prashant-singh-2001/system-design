package sd.p04.day32;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class Day32LruCacheTest {

    @Test
    @DisplayName("basic put/get round trip")
    void putThenGet() {
        LruCache<String, String> cache = new LruCache<>(3);

        cache.put("a", "1");

        assertThat(cache.get("a")).contains("1");
    }

    @Test
    @DisplayName("a missing key returns empty, not null or an exception")
    void missingKeyIsEmpty() {
        assertThat(new LruCache<String, String>(3).get("nope")).isEmpty();
    }

    @Test
    @DisplayName("putting beyond capacity evicts the least recently used entry")
    void evictsLeastRecentlyUsed() {
        LruCache<String, String> cache = new LruCache<>(2);

        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");        // capacity 2: "a" was least recently touched

        assertThat(cache.get("a")).isEmpty();
        assertThat(cache.get("b")).contains("2");
        assertThat(cache.get("c")).contains("3");
    }

    @Test
    @DisplayName("reading an entry protects it from eviction - THE defining LRU behaviour")
    void readingProtectsFromEviction() {
        LruCache<String, String> cache = new LruCache<>(2);
        cache.put("a", "1");
        cache.put("b", "2");

        cache.get("a");              // "a" is now the most recently used
        cache.put("c", "3");         // capacity 2: "b" is now the least recently used

        assertThat(cache.get("b")).as("b was untouched since a's read, so it is evicted").isEmpty();
        assertThat(cache.get("a")).contains("1");
        assertThat(cache.get("c")).contains("3");
    }

    @Test
    @DisplayName("updating an existing key counts as a use and does not grow the cache")
    void updatingExistingKeyIsAUse() {
        LruCache<String, String> cache = new LruCache<>(2);
        cache.put("a", "1");
        cache.put("b", "2");

        cache.put("a", "1-updated");  // touches "a" without adding a new entry
        cache.put("c", "3");          // "b" is now least recently used, not "a"

        assertThat(cache.get("a")).contains("1-updated");
        assertThat(cache.get("b")).isEmpty();
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("size never exceeds capacity, however many entries are inserted")
    void sizeIsCapped() {
        LruCache<Integer, Integer> cache = new LruCache<>(3);

        for (int i = 0; i < 20; i++) {
            cache.put(i, i);
        }

        assertThat(cache.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("concurrent access from many threads never corrupts the cache")
    void survivesConcurrentHammering() throws InterruptedException {
        int capacity = 16;
        LruCache<Integer, Integer> cache = new LruCache<>(capacity);
        int threadCount = 8;
        int opsPerThread = 2_000;
        AtomicInteger failures = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int t = 0; t < threadCount; t++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        int key = i % (capacity * 2);
                        cache.put(key, key);
                        cache.get(key);
                    }
                } catch (RuntimeException e) {
                    failures.incrementAndGet();
                }
            });
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        assertThat(failures)
                .as("no thread should have hit an exception from a corrupted internal structure")
                .hasValue(0);
        assertThat(cache.size())
                .as("the invariant that must hold no matter how many threads hammered it")
                .isLessThanOrEqualTo(capacity);
    }
}
