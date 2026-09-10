package sd.p06.day54;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/** GIVEN - the expensive thing behind the cache. It counts calls and takes real time. */
public final class ExpensiveSource {

    private final AtomicInteger calls = new AtomicInteger();
    private final Duration cost;

    public ExpensiveSource(Duration cost) {
        this.cost = cost;
    }

    public String load(String key) {
        calls.incrementAndGet();
        try {
            Thread.sleep(cost.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "value-for-" + key;
    }

    public int calls() {
        return calls.get();
    }

    public void reset() {
        calls.set(0);
    }
}
