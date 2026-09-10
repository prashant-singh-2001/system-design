package sd.p04.day39;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day39ExpiringStoreTest {

    @Test
    @DisplayName("put then get returns the value before expiry")
    void putThenGetBeforeExpiry() {
        TestClock clock = new TestClock();
        ExpiringStore<String, String> store = new ExpiringStore<>(clock, 10);

        store.put("a", "1", Duration.ofSeconds(60));

        assertThat(store.get("a")).contains("1");
    }

    @Test
    @DisplayName("LAZY expiry: a read after the TTL has passed treats the key as absent")
    void lazyExpiryOnRead() {
        TestClock clock = new TestClock();
        ExpiringStore<String, String> store = new ExpiringStore<>(clock, 10);
        store.put("a", "1", Duration.ofSeconds(30));

        clock.advance(Duration.ofSeconds(31));

        assertThat(store.get("a")).isEmpty();
        assertThat(store.size()).isZero();
    }

    @Test
    @DisplayName("a non-positive TTL is rejected")
    void nonPositiveTtlIsRejected() {
        TestClock clock = new TestClock();
        ExpiringStore<String, String> store = new ExpiringStore<>(clock, 10);

        assertThatThrownBy(() -> store.put("a", "1", Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> store.put("a", "1", Duration.ofSeconds(-5)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a permanent entry survives an arbitrarily large clock advance")
    void permanentEntryNeverExpires() {
        TestClock clock = new TestClock();
        ExpiringStore<String, String> store = new ExpiringStore<>(clock, 10);
        store.putPermanent("a", "1");

        clock.advance(Duration.ofDays(365 * 100));

        assertThat(store.get("a")).contains("1");
    }

    @Test
    @DisplayName("ACTIVE sweep removes expired entries even without anyone reading them")
    void activeSweepRemovesExpiredEntries() {
        TestClock clock = new TestClock();
        ExpiringStore<String, String> store = new ExpiringStore<>(clock, 10);
        store.put("a", "1", Duration.ofSeconds(10));
        store.put("b", "2", Duration.ofSeconds(100));

        clock.advance(Duration.ofSeconds(20));
        int removed = store.sweepExpired();

        assertThat(removed).isEqualTo(1);
        assertThat(store.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("size() always reports only the currently-live entries")
    void sizeReflectsLiveEntriesOnly() {
        TestClock clock = new TestClock();
        ExpiringStore<String, String> store = new ExpiringStore<>(clock, 10);
        store.put("a", "1", Duration.ofSeconds(10));
        store.put("b", "2", Duration.ofSeconds(10));
        store.putPermanent("c", "3");

        clock.advance(Duration.ofSeconds(20));

        assertThat(store.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("EVICTION: at capacity, the entry with the soonest expiry is evicted for a new key")
    void evictsSoonestToExpireEntry() {
        TestClock clock = new TestClock();
        ExpiringStore<String, String> store = new ExpiringStore<>(clock, 2);
        store.put("soon", "1", Duration.ofSeconds(10));
        store.put("later", "2", Duration.ofSeconds(1_000));

        store.put("newcomer", "3", Duration.ofSeconds(500));

        assertThat(store.get("soon")).as("closest to expiring - evicted to make room").isEmpty();
        assertThat(store.get("later")).contains("2");
        assertThat(store.get("newcomer")).contains("3");
        assertThat(store.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("EVICTION: a permanent entry is not evicted while a TTL'd entry is available instead")
    void permanentEntryIsPreferredOverTtlEntryWhenEvicting() {
        TestClock clock = new TestClock();
        ExpiringStore<String, String> store = new ExpiringStore<>(clock, 2);
        store.putPermanent("forever", "1");
        store.put("expiring", "2", Duration.ofSeconds(10));

        store.put("newcomer", "3", Duration.ofSeconds(500));

        assertThat(store.get("forever")).as("permanent entries are the last resort for eviction").contains("1");
        assertThat(store.get("expiring")).isEmpty();
    }

    @Test
    @DisplayName("updating an existing key is not a capacity event and evicts nothing")
    void updatingExistingKeyDoesNotEvict() {
        TestClock clock = new TestClock();
        ExpiringStore<String, String> store = new ExpiringStore<>(clock, 2);
        store.put("a", "1", Duration.ofSeconds(10));
        store.put("b", "2", Duration.ofSeconds(1_000));

        store.put("a", "1-updated", Duration.ofSeconds(10));   // same key, not new

        assertThat(store.get("a")).contains("1-updated");
        assertThat(store.get("b")).as("b must not have been evicted by updating a").contains("2");
        assertThat(store.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("remove() deletes a key outright, independent of its expiry")
    void removeDeletesRegardlessOfExpiry() {
        TestClock clock = new TestClock();
        ExpiringStore<String, String> store = new ExpiringStore<>(clock, 10);
        store.putPermanent("a", "1");

        store.remove("a");

        assertThat(store.get("a")).isEmpty();
    }
}
