package sd.p04.day33;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day33RateLimiterTest {

    @Test
    @DisplayName("TOKEN BUCKET: starts full, so it allows an immediate burst up to capacity")
    void tokenBucketAllowsInitialBurst() {
        TestClock clock = new TestClock();
        RateLimiter limiter = new TokenBucketRateLimiter(5, 1.0, clock);

        for (int i = 0; i < 5; i++) {
            assertThat(limiter.tryAcquire()).as("request #%d of the burst", i + 1).isTrue();
        }
        assertThat(limiter.tryAcquire()).as("the 6th request exceeds capacity").isFalse();
    }

    @Test
    @DisplayName("TOKEN BUCKET: refills continuously at the configured rate")
    void tokenBucketRefillsOverTime() {
        TestClock clock = new TestClock();
        RateLimiter limiter = new TokenBucketRateLimiter(5, 1.0, clock);
        for (int i = 0; i < 5; i++) {
            limiter.tryAcquire();
        }
        assertThat(limiter.tryAcquire()).isFalse();

        clock.advance(Duration.ofSeconds(1));

        assertThat(limiter.tryAcquire())
                .as("one second at 1 token/second must have refilled exactly one token")
                .isTrue();
        assertThat(limiter.tryAcquire()).as("and only one").isFalse();
    }

    @Test
    @DisplayName("TOKEN BUCKET: refill never exceeds capacity, even after a long idle period")
    void tokenBucketCapsAtCapacity() {
        TestClock clock = new TestClock();
        RateLimiter limiter = new TokenBucketRateLimiter(5, 1.0, clock);
        for (int i = 0; i < 5; i++) {
            limiter.tryAcquire();
        }

        clock.advance(Duration.ofSeconds(1_000));   // would be 1000 tokens without the cap

        int allowed = 0;
        for (int i = 0; i < 10; i++) {
            if (limiter.tryAcquire()) {
                allowed++;
            }
        }
        assertThat(allowed)
                .as("capacity must cap the backlog - a long idle period is not an unlimited burst")
                .isEqualTo(5);
    }

    @Test
    @DisplayName("SLIDING WINDOW: allows up to maxRequests within the window, then blocks")
    void slidingWindowEnforcesTheLimit() {
        TestClock clock = new TestClock();
        RateLimiter limiter = new SlidingWindowRateLimiter(3, Duration.ofSeconds(1), clock);

        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).as("the 4th request within the window is refused").isFalse();
    }

    @Test
    @DisplayName("SLIDING WINDOW: capacity returns once old requests age out of the window")
    void slidingWindowFreesUpAfterTheWindowPasses() {
        TestClock clock = new TestClock();
        RateLimiter limiter = new SlidingWindowRateLimiter(2, Duration.ofSeconds(1), clock);
        limiter.tryAcquire();
        limiter.tryAcquire();
        assertThat(limiter.tryAcquire()).isFalse();

        clock.advance(Duration.ofSeconds(2));   // well past the window

        assertThat(limiter.tryAcquire())
                .as("the earlier requests are now outside the trailing window")
                .isTrue();
    }

    @Test
    @DisplayName("SLIDING WINDOW: a request that ages out does not count against a later one")
    void slidingWindowOnlyCountsRequestsInsideTheWindow() {
        TestClock clock = new TestClock();
        RateLimiter limiter = new SlidingWindowRateLimiter(1, Duration.ofSeconds(1), clock);

        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isFalse();

        clock.advance(Duration.ofMillis(1_500));

        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isFalse();
    }

    @Test
    @DisplayName("both limiters reject nonsensical configuration")
    void invalidConfigurationIsRejected() {
        TestClock clock = new TestClock();

        assertThatThrownBy(() -> new TokenBucketRateLimiter(0, 1.0, clock))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SlidingWindowRateLimiter(0, Duration.ofSeconds(1), clock))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
