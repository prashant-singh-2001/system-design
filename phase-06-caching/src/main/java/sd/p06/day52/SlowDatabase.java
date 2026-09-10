package sd.p06.day52;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/** GIVEN - a database that counts every call, so tests can assert on round trips. */
public final class SlowDatabase {

    private final Map<String, String> rows = new HashMap<>();
    private final AtomicInteger reads = new AtomicInteger();
    private final AtomicInteger writes = new AtomicInteger();

    public Optional<String> read(String key) {
        reads.incrementAndGet();
        return Optional.ofNullable(rows.get(key));
    }

    public void write(String key, String value) {
        writes.incrementAndGet();
        rows.put(key, value);
    }

    /** Seeds a row without counting it, so tests can arrange state. */
    public void seed(String key, String value) {
        rows.put(key, value);
    }

    public int reads() {
        return reads.get();
    }

    public int writes() {
        return writes.get();
    }

    public void resetCounters() {
        reads.set(0);
        writes.set(0);
    }
}
