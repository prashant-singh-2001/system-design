package sd.p02.day13;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GIVEN - the contract from {@link KeyValueStore}, expressed as executable tests.
 *
 * <p>This is a CONTRACT TEST: written once against the interface, inherited by every
 * implementation. It is the only practical way to enforce Liskov substitutability, because
 * the compiler cannot check behaviour - only shape.
 *
 * <p>Abstract, so JUnit does not run it directly. Each implementation gets a small subclass
 * that supplies an instance.
 */
abstract class KeyValueStoreContract {

    /** Implementations supply an empty store with room for at least three entries. */
    abstract KeyValueStore newStore();

    @Test
    @DisplayName("contract 1: after put, get returns the value")
    void putThenGet() {
        KeyValueStore store = newStore();
        store.put("a", "1");

        assertThat(store.get("a")).contains("1");
    }

    @Test
    @DisplayName("contract 2: put overwrites an existing key")
    void putOverwrites() {
        KeyValueStore store = newStore();
        store.put("a", "1");
        store.put("a", "2");

        assertThat(store.get("a")).contains("2");
        assertThat(store.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("contract 3: an unknown key returns empty, not null")
    void missingKeyIsEmpty() {
        assertThat(newStore().get("nope")).isEmpty();
    }

    @Test
    @DisplayName("contract 4: size counts retrievable entries")
    void sizeReflectsContents() {
        KeyValueStore store = newStore();
        assertThat(store.size()).isZero();

        store.put("a", "1");
        store.put("b", "2");

        assertThat(store.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("contract 1, under pressure: a put is NEVER silently dropped")
    void everyPutIsImmediatelyReadable() {
        KeyValueStore store = newStore();

        // Deliberately write past any plausible capacity. Evicting old keys is allowed.
        // Accepting a write and not storing it is not.
        for (int i = 0; i < 20; i++) {
            String key = "key-" + i;
            store.put(key, "value-" + i);

            assertThat(store.get(key))
                    .as("""
                            put("%s") returned normally, so get("%s") must return its value.
                            An implementation may evict OLDER entries to make room. It may never
                            accept a write and then not have it.""", key, key)
                    .contains("value-" + i);
        }
    }
}
