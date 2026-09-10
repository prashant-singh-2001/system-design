package sd.p08.day73;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sd.p08.support.MutableClock;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day73CircuitBreakerTest {

    private static final Duration OPEN_FOR = Duration.ofSeconds(30);

    private MutableClock clock() {
        return MutableClock.startingAt("2026-03-01T12:00:00Z");
    }

    private CircuitBreaker breaker(MutableClock clock) {
        return new CircuitBreaker(3, OPEN_FOR, 2, clock);
    }

    private static final Callable<String> FAILS = () -> {
        throw new IllegalStateException("dependency is down");
    };
    private static final Callable<String> SUCCEEDS = () -> "ok";

    @Test
    @DisplayName("a healthy dependency passes straight through")
    void closedPassesThrough() {
        CircuitBreaker breaker = breaker(clock());

        for (int i = 0; i < 100; i++) {
            assertThat(breaker.call(SUCCEEDS)).isEqualTo("ok");
        }

        assertThat(breaker.state()).isEqualTo(CircuitState.CLOSED);
        assertThat(breaker.rejectedCalls()).isZero();
    }

    @Test
    @DisplayName("consecutive failures trip the breaker")
    void tripsAfterThreshold() {
        CircuitBreaker breaker = breaker(clock());

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> breaker.call(FAILS)).isInstanceOf(IllegalStateException.class);
        }

        assertThat(breaker.state()).isEqualTo(CircuitState.OPEN);
    }

    @Test
    @DisplayName("an open breaker fails fast, without touching the dependency")
    void openFailsFast() {
        CircuitBreaker breaker = breaker(clock());
        AtomicInteger dependencyCalls = new AtomicInteger();

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> breaker.call(FAILS)).isInstanceOf(IllegalStateException.class);
        }

        for (int i = 0; i < 50; i++) {
            assertThatThrownBy(() -> breaker.call(() -> {
                dependencyCalls.incrementAndGet();
                return "ok";
            })).isInstanceOf(CircuitBreakerOpenException.class);
        }

        assertThat(dependencyCalls)
                .as("""
                        Fifty requests, zero calls to a dependency we believe is down. Each one
                        failed in microseconds instead of parking a thread on a timeout - which is
                        what stops one sick service from taking down a healthy one.""")
                .hasValue(0);
        assertThat(breaker.rejectedCalls()).isEqualTo(50);
    }

    @Test
    @DisplayName("a success resets the failure count - only CONSECUTIVE failures trip it")
    void successResetsTheCount() {
        CircuitBreaker breaker = breaker(clock());

        assertThatThrownBy(() -> breaker.call(FAILS)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> breaker.call(FAILS)).isInstanceOf(IllegalStateException.class);
        breaker.call(SUCCEEDS);
        assertThatThrownBy(() -> breaker.call(FAILS)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> breaker.call(FAILS)).isInstanceOf(IllegalStateException.class);

        assertThat(breaker.state())
                .as("occasional failures are normal; a run of them is a signal")
                .isEqualTo(CircuitState.CLOSED);
    }

    @Test
    @DisplayName("after the open period, one probe is allowed through")
    void halfOpenProbes() {
        MutableClock clock = clock();
        CircuitBreaker breaker = breaker(clock);

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> breaker.call(FAILS)).isInstanceOf(IllegalStateException.class);
        }
        assertThat(breaker.state()).isEqualTo(CircuitState.OPEN);

        clock.advance(Duration.ofSeconds(31));

        assertThat(breaker.call(SUCCEEDS)).as("a probe gets through").isEqualTo("ok");
        assertThat(breaker.state()).isEqualTo(CircuitState.HALF_OPEN);
    }

    @Test
    @DisplayName("enough successful probes close the breaker")
    void recovery() {
        MutableClock clock = clock();
        CircuitBreaker breaker = breaker(clock);

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> breaker.call(FAILS)).isInstanceOf(IllegalStateException.class);
        }
        clock.advance(Duration.ofSeconds(31));

        breaker.call(SUCCEEDS);
        breaker.call(SUCCEEDS);

        assertThat(breaker.state()).isEqualTo(CircuitState.CLOSED);
        assertThat(breaker.call(SUCCEEDS)).isEqualTo("ok");
    }

    @Test
    @DisplayName("a failed probe reopens the breaker and restarts the timer")
    void failedProbeReopens() {
        MutableClock clock = clock();
        CircuitBreaker breaker = breaker(clock);

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> breaker.call(FAILS)).isInstanceOf(IllegalStateException.class);
        }
        clock.advance(Duration.ofSeconds(31));

        assertThatThrownBy(() -> breaker.call(FAILS)).isInstanceOf(IllegalStateException.class);

        assertThat(breaker.state())
                .as("""
                        Without this, a breaker would slam the full load back onto a service that
                        has only just come up - and knock it straight over again.""")
                .isEqualTo(CircuitState.OPEN);

        assertThatThrownBy(() -> breaker.call(SUCCEEDS))
                .isInstanceOf(CircuitBreakerOpenException.class);
    }

    @Test
    @DisplayName("the breaker stays open until its timer expires")
    void staysOpenBeforeTheTimer() {
        MutableClock clock = clock();
        CircuitBreaker breaker = breaker(clock);

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> breaker.call(FAILS)).isInstanceOf(IllegalStateException.class);
        }

        clock.advance(Duration.ofSeconds(29));

        assertThatThrownBy(() -> breaker.call(SUCCEEDS))
                .isInstanceOf(CircuitBreakerOpenException.class);
    }

    @Test
    @DisplayName("a breaker needs a sane configuration")
    void validation() {
        assertThatThrownBy(() -> new CircuitBreaker(0, OPEN_FOR, 2, clock()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CircuitBreaker(3, OPEN_FOR, 0, clock()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
