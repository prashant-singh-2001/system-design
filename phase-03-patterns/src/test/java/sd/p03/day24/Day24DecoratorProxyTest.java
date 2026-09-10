package sd.p03.day24;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day24DecoratorProxyTest {

    @Test
    @DisplayName("TimingDecorator reports a non-negative duration on success")
    void timingMeasuresSuccess() {
        List<Duration> measured = new ArrayList<>();
        SlowService timed = new TimingDecorator(key -> "value-for-" + key, measured::add);

        String result = timed.fetch("a");

        assertThat(result).isEqualTo("value-for-a");
        assertThat(measured).hasSize(1);
        assertThat(measured.get(0)).isGreaterThanOrEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("TimingDecorator still measures a call that throws, then rethrows unchanged")
    void timingMeasuresFailureToo() {
        List<Duration> measured = new ArrayList<>();
        SlowService alwaysFails = key -> {
            throw new RuntimeException("boom");
        };
        SlowService timed = new TimingDecorator(alwaysFails, measured::add);

        assertThatThrownBy(() -> timed.fetch("a"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");
        assertThat(measured)
                .as("a timing decorator that goes blind on failure is worse than useless")
                .hasSize(1);
    }

    @Test
    @DisplayName("CachingDecorator serves a second call from cache, never touching the delegate")
    void cachingAvoidsRepeatedCalls() {
        FlakyRemoteService remote = new FlakyRemoteService(0);
        SlowService cached = new CachingDecorator(remote);

        assertThat(cached.fetch("a")).isEqualTo("value-for-a");
        assertThat(cached.fetch("a")).isEqualTo("value-for-a");

        assertThat(remote.callCount()).as("the second fetch must be a pure cache hit").isEqualTo(1);
    }

    @Test
    @DisplayName("CachingDecorator does not cache a failed attempt")
    void cachingDoesNotRememberFailures() {
        FlakyRemoteService remote = new FlakyRemoteService(1);
        SlowService cached = new CachingDecorator(remote);

        assertThatThrownBy(() -> cached.fetch("a")).isInstanceOf(RuntimeException.class);
        assertThat(cached.fetch("a")).isEqualTo("value-for-a");
        assertThat(remote.callCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("RetryDecorator absorbs failures within its attempt budget")
    void retrySucceedsWithinBudget() {
        FlakyRemoteService remote = new FlakyRemoteService(2);
        SlowService retrying = new RetryDecorator(remote, 3);

        assertThat(retrying.fetch("a")).isEqualTo("value-for-a");
        assertThat(remote.callCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("RetryDecorator gives up after maxAttempts and rethrows the last failure")
    void retryExhaustsAndRethrows() {
        FlakyRemoteService remote = new FlakyRemoteService(10);
        SlowService retrying = new RetryDecorator(remote, 3);

        assertThatThrownBy(() -> retrying.fetch("a")).isInstanceOf(RuntimeException.class);
        assertThat(remote.callCount())
                .as("exactly maxAttempts calls - not one more, not one fewer")
                .isEqualTo(3);
    }

    @Test
    @DisplayName("RetryDecorator refuses a non-positive attempt budget")
    void retryRejectsInvalidBudget() {
        assertThatThrownBy(() -> new RetryDecorator(key -> key, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the full stack: timing wraps caching wraps retry wraps the flaky remote")
    void fullStackComposesCorrectly() {
        FlakyRemoteService remote = new FlakyRemoteService(1);
        List<Duration> measured = new ArrayList<>();
        SlowService stack = new TimingDecorator(
                new CachingDecorator(
                        new RetryDecorator(remote, 3)),
                measured::add);

        String first = stack.fetch("a");
        String second = stack.fetch("a");

        assertThat(first).isEqualTo("value-for-a");
        assertThat(second).isEqualTo("value-for-a");
        assertThat(remote.callCount())
                .as("retry absorbed the one failure; caching then made the second call free")
                .isEqualTo(2);
        assertThat(measured)
                .as("timing sits outermost, so BOTH calls get measured even though one was a cache hit")
                .hasSize(2);
    }

    @Test
    @DisplayName("LazyServiceProxy defers construction until the first call")
    void lazyProxyDefersConstruction() {
        AtomicInteger factoryInvocations = new AtomicInteger(0);
        SlowService proxy = new LazyServiceProxy(() -> {
            factoryInvocations.incrementAndGet();
            return key -> "value-for-" + key;
        });

        assertThat(factoryInvocations).as("nothing should be built yet").hasValue(0);

        assertThat(proxy.fetch("a")).isEqualTo("value-for-a");
        assertThat(proxy.fetch("b")).isEqualTo("value-for-b");

        assertThat(factoryInvocations)
                .as("the real service is built exactly once, no matter how many calls follow")
                .hasValue(1);
    }
}
