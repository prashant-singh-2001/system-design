package sd.p02.day13;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The same contract, applied to the bounded implementation. Red until you fix
 * {@link BoundedStore}.
 *
 * <p>Note how little code it took to hold a second implementation to the same standard.
 * That is the argument for contract tests in one sentence.
 */
class Day13BoundedStoreTest extends KeyValueStoreContract {

    @Override
    KeyValueStore newStore() {
        return new BoundedStore(3);
    }

    @Test
    @DisplayName("bounded means bounded - it may evict, but it must stay within capacity")
    void respectsItsBound() {
        KeyValueStore store = new BoundedStore(3);
        for (int i = 0; i < 10; i++) {
            store.put("k" + i, "v" + i);
        }

        assertThat(store.size())
                .as("the whole reason this class exists is to cap memory")
                .isEqualTo(3);
    }

    @Test
    @DisplayName("eviction takes the oldest, so recent writes survive")
    void evictsOldestFirst() {
        KeyValueStore store = new BoundedStore(3);
        store.put("a", "1");
        store.put("b", "2");
        store.put("c", "3");
        store.put("d", "4");        // pushes out "a"

        assertThat(store.get("a")).as("oldest entry evicted - this is allowed").isEmpty();
        assertThat(store.get("d")).as("newest entry must be present").contains("4");
        assertThat(store.get("c")).contains("3");
    }
}
