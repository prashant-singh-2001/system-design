package sd.p09.day83;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sd.p09.support.MutableClock;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day83RateLimiterTest {

    private static final Duration MINUTE = Duration.ofMinutes(1);

    private MutableClock clock;
    private DistributedRateLimiter limiter;

    @BeforeEach
    void setUp() {
        clock = MutableClock.startingAt("2026-03-01T12:00:00Z");
        limiter = new DistributedRateLimiter(5, MINUTE, clock);
    }

    @Test
    @DisplayName("requests within the limit are allowed, and the remainder counts down")
    void withinLimit() {
        for (int i = 0; i < 5; i++) {
            RateLimitDecision decision = limiter.check("client-a");

            assertThat(decision.allowed()).isTrue();
            assertThat(decision.remaining()).isEqualTo(4 - i);
        }
    }

    @Test
    @DisplayName("the sixth request in the window is denied")
    void overLimit() {
        for (int i = 0; i < 5; i++) {
            limiter.check("client-a");
        }

        RateLimitDecision denied = limiter.check("client-a");

        assertThat(denied.allowed()).isFalse();
        assertThat(denied.remaining()).isZero();
    }

    @Test
    @DisplayName("retry-after names the exact moment a slot frees")
    void retryAfterIsPrecise() {
        limiter.check("client-a");
        clock.advance(Duration.ofSeconds(10));
        for (int i = 0; i < 4; i++) {
            limiter.check("client-a");
        }

        RateLimitDecision denied = limiter.check("client-a");

        assertThat(denied.retryAfter())
                .as("""
                        The oldest request was 10 seconds ago, so it leaves the window in 50. Giving
                        the client that number is the difference between one that backs off
                        correctly and one that spins.""")
                .isEqualTo(Duration.ofSeconds(50));
    }

    @Test
    @DisplayName("the window slides - old requests stop counting")
    void windowSlides() {
        for (int i = 0; i < 5; i++) {
            limiter.check("client-a");
        }
        assertThat(limiter.check("client-a").allowed()).isFalse();

        clock.advance(Duration.ofSeconds(61));

        assertThat(limiter.check("client-a").allowed())
                .as("every earlier request has fallen out of the window")
                .isTrue();
    }

    @Test
    @DisplayName("THE flaw a fixed window has and this does not")
    void noBoundaryDoubling() {
        // Five requests at the very end of a notional fixed minute.
        clock.advance(Duration.ofSeconds(59));
        for (int i = 0; i < 5; i++) {
            assertThat(limiter.check("client-a").allowed()).isTrue();
        }

        // Two seconds later - a new fixed window would have reset and allowed five more.
        clock.advance(Duration.ofSeconds(2));

        assertThat(limiter.check("client-a").allowed())
                .as("""
                        A fixed-window counter would allow ten requests in three seconds against a
                        five-per-minute limit. If the limiter exists to protect a downstream
                        service, that is a failure at exactly the moment it mattered.""")
                .isFalse();
    }

    @Test
    @DisplayName("clients are limited independently")
    void perClientLimits() {
        for (int i = 0; i < 5; i++) {
            limiter.check("client-a");
        }

        assertThat(limiter.check("client-a").allowed()).isFalse();
        assertThat(limiter.check("client-b").allowed())
                .as("one noisy client must not consume another's allowance")
                .isTrue();
    }

    @Test
    @DisplayName("memory is the price of exactness - old timestamps are evicted, not hoarded")
    void memoryIsBounded() {
        for (int i = 0; i < 5; i++) {
            limiter.check("client-a");
        }
        assertThat(limiter.trackedRequests("client-a")).isEqualTo(5);

        clock.advance(Duration.ofSeconds(61));
        limiter.check("client-a");

        assertThat(limiter.trackedRequests("client-a"))
                .as("""
                        The log stays proportional to the limit, not to total traffic. That bound
                        is why a sliding-window LOG is affordable at all - and why very high limits
                        push you toward the approximate sliding-window counter instead.""")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("a limit below one is not a limit")
    void validation() {
        assertThatThrownBy(() -> new DistributedRateLimiter(0, MINUTE, clock))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
