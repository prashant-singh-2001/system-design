package sd.p08.day80;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.random.RandomGenerator;

/** GIVEN - a dependency that fails a configurable fraction of the time, and counts its calls. */
public final class FlakyDependency {

    private final double failureRate;
    private final RandomGenerator random;
    private final AtomicInteger calls = new AtomicInteger();
    private volatile boolean hardDown;

    public FlakyDependency(double failureRate, RandomGenerator random) {
        this.failureRate = failureRate;
        this.random = random;
    }

    public String call(String input) {
        calls.incrementAndGet();
        if (hardDown || random.nextDouble() < failureRate) {
            throw new IllegalStateException("dependency failed");
        }
        return "handled:" + input;
    }

    /** Simulate a total outage. */
    public void goDown() {
        hardDown = true;
    }

    public void recover() {
        hardDown = false;
    }

    public int calls() {
        return calls.get();
    }
}
