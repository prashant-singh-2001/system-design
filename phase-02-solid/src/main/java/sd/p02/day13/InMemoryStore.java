package sd.p02.day13;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** GIVEN - the reference implementation. Unbounded, and trivially correct. */
public final class InMemoryStore implements KeyValueStore {

    private final Map<String, String> entries = new HashMap<>();

    @Override
    public void put(String key, String value) {
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
