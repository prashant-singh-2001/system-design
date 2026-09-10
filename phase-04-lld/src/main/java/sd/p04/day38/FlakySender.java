package sd.p04.day38;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * GIVEN - a controllable, deterministic failure for exercising {@link NotificationDispatcher}'s
 * retry logic, the same shape as Phase 3 Day 24's {@code FlakyRemoteService}.
 */
public final class FlakySender implements NotificationSender {

    private final int failuresBeforeSuccess;
    private final AtomicInteger callCount = new AtomicInteger(0);

    public FlakySender(int failuresBeforeSuccess) {
        this.failuresBeforeSuccess = failuresBeforeSuccess;
    }

    @Override
    public void send(Notification notification) {
        int callNumber = callCount.incrementAndGet();
        if (callNumber <= failuresBeforeSuccess) {
            throw new NotificationException("simulated failure #" + callNumber);
        }
    }

    public int callCount() {
        return callCount.get();
    }
}
