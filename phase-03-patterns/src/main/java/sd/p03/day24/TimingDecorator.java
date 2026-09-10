package sd.p03.day24;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * TODO(day24): a DECORATOR - same interface as what it wraps, adding one concern (measurement)
 * without the caller ever knowing it is there.
 *
 * <p>Record how long {@code delegate.fetch(key)} takes and pass the elapsed {@link Duration} to
 * {@code onMeasured}, then return whatever the delegate returned. Measure it whether the call
 * SUCCEEDS or THROWS - a timing decorator that only reports successes is blind to the calls that
 * are timing out or failing slowly, which are usually the ones you most need to see. Use a
 * {@code try/finally} and rethrow whatever the delegate threw, unchanged.
 */
public final class TimingDecorator implements SlowService {

    public TimingDecorator(SlowService delegate, Consumer<Duration> onMeasured) {
        throw new UnsupportedOperationException("TODO(day24): store both collaborators");
    }

    @Override
    public String fetch(String key) {
        throw new UnsupportedOperationException(
                "TODO(day24): time the call in a finally block, then return or rethrow");
    }
}
