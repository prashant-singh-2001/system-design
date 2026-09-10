package sd.p03.day26;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class Day26FilterChainTest {

    private static Handler countingTerminal(AtomicInteger calls) {
        return request -> {
            calls.incrementAndGet();
            return new Response(200, "ok:" + request.path());
        };
    }

    @Test
    @DisplayName("a valid key under the rate limit reaches the terminal handler")
    void happyPathReachesHandler() {
        AtomicInteger terminalCalls = new AtomicInteger(0);
        Handler pipeline = FilterChain.build(
                List.of(new AuthenticationFilter(Set.of("good-key")), new RateLimitFilter(5)),
                countingTerminal(terminalCalls));

        Response response = pipeline.handle(new Request("good-key", "1.2.3.4", "/orders"));

        assertThat(response).isEqualTo(new Response(200, "ok:/orders"));
        assertThat(terminalCalls).hasValue(1);
    }

    @Test
    @DisplayName("an invalid key is rejected before the terminal handler ever runs")
    void invalidKeyIsRejected() {
        AtomicInteger terminalCalls = new AtomicInteger(0);
        Handler pipeline = FilterChain.build(
                List.of(new AuthenticationFilter(Set.of("good-key")), new RateLimitFilter(5)),
                countingTerminal(terminalCalls));

        Response response = pipeline.handle(new Request("bad-key", "1.2.3.4", "/orders"));

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(terminalCalls).as("the endpoint must never run for a rejected request").hasValue(0);
    }

    @Test
    @DisplayName("exceeding the rate limit is rejected before the terminal handler runs")
    void rateLimitIsEnforced() {
        AtomicInteger terminalCalls = new AtomicInteger(0);
        Handler pipeline = FilterChain.build(
                List.of(new AuthenticationFilter(Set.of("good-key")), new RateLimitFilter(2)),
                countingTerminal(terminalCalls));
        Request request = new Request("good-key", "9.9.9.9", "/orders");

        pipeline.handle(request);
        pipeline.handle(request);
        Response third = pipeline.handle(request);

        assertThat(third.statusCode()).isEqualTo(429);
        assertThat(terminalCalls).hasValue(2);
    }

    @Test
    @DisplayName("filter order matters: authentication runs before rate limiting")
    void authenticationRunsFirst() {
        AtomicInteger terminalCalls = new AtomicInteger(0);
        Handler pipeline = FilterChain.build(
                List.of(new AuthenticationFilter(Set.of("good-key")), new RateLimitFilter(0)),
                countingTerminal(terminalCalls));

        // Both filters would reject this request - a bad key AND zero rate-limit budget.
        // Registration order says authentication runs first, so 401 must win, not 429.
        Response response = pipeline.handle(new Request("bad-key", "9.9.9.9", "/orders"));

        assertThat(response.statusCode())
                .as("with authentication registered first, it must fail before rate limiting runs")
                .isEqualTo(401);
    }

    @Test
    @DisplayName("a new cross-cutting filter slots in without touching the existing ones")
    void newFilterComposesWithoutModifyingExistingOnes() {
        AtomicInteger terminalCalls = new AtomicInteger(0);
        StringBuilder log = new StringBuilder();
        Filter loggingFilter = (request, next) -> {
            log.append("path=").append(request.path()).append(';');
            return next.handle(request);
        };

        Handler pipeline = FilterChain.build(
                List.of(loggingFilter, new AuthenticationFilter(Set.of("good-key"))),
                countingTerminal(terminalCalls));

        pipeline.handle(new Request("good-key", "1.2.3.4", "/health"));

        assertThat(log.toString()).isEqualTo("path=/health;");
        assertThat(terminalCalls).hasValue(1);
    }
}
