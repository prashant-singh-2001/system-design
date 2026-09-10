package sd.p02.day13;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * GIVEN, and BROKEN. This is today's exercise.
 *
 * <p>It is bounded, which is fine and necessary - an unbounded cache is an outage waiting to
 * happen. The bug is what it does when full: it silently ignores the write.
 *
 * <p>Read that again. {@code put} returns normally. No exception, no return value, no log.
 * The caller has every reason to believe the value was stored. It was not.
 *
 * <p>This is a Liskov violation with teeth. Anywhere a {@code KeyValueStore} is expected, this
 * class can be substituted - it compiles, it has the right shape - and it will quietly lose
 * data under exactly the conditions you built it for. Type systems do not catch this. Only a
 * written contract and a shared contract test do.
 *
 * <p>TODO(day13): make it honour the contract. Eviction is allowed; silent data loss is not.
 * Evict the oldest entry to make room, then store the new one. {@code LinkedHashMap} with
 * access-order or insertion-order iteration gives you the oldest key cheaply.
 */
public final class BoundedStore implements KeyValueStore {

    private final int capacity;
    private final Map<String, String> entries = new LinkedHashMap<>();

    public BoundedStore(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        this.capacity = capacity;
    }

    @Override
    public void put(String key, String value) {
        if (entries.size() >= capacity && !entries.containsKey(key)) {
            return;                 // <-- the bug. Accepts the call, drops the data.
        }
        entries.put(key, value);
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(entries.get(key));
    }

    @Override
    public int size() {
        return entries.size();
    }
}
