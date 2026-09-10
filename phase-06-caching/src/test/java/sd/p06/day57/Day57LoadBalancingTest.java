package sd.p06.day57;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day57LoadBalancingTest {

    private static List<Backend> three() {
        return List.of(new Backend("a"), new Backend("b"), new Backend("c"));
    }

    static Stream<Function<List<Backend>, LoadBalancer>> allBalancers() {
        return Stream.of(
                RoundRobinBalancer::new,
                WeightedRoundRobinBalancer::new,
                LeastConnectionsBalancer::new,
                HashBalancer::new);
    }

    @ParameterizedTest(name = "{index}: never returns an unhealthy backend")
    @MethodSource("allBalancers")
    void skipsUnhealthyBackends(Function<List<Backend>, LoadBalancer> factory) {
        List<Backend> backends = new ArrayList<>(three());
        backends.get(1).markUnhealthy();
        LoadBalancer balancer = factory.apply(backends);

        for (int i = 0; i < 50; i++) {
            assertThat(balancer.choose("client-" + i).id())
                    .as("%s routed to a dead backend", balancer.name())
                    .isNotEqualTo("b");
        }
    }

    @ParameterizedTest(name = "{index}: an all-dead fleet fails loudly")
    @MethodSource("allBalancers")
    void allUnhealthyThrows(Function<List<Backend>, LoadBalancer> factory) {
        List<Backend> backends = new ArrayList<>(three());
        backends.forEach(Backend::markUnhealthy);

        assertThatThrownBy(() -> factory.apply(backends).choose("client"))
                .as("silently returning a dead backend turns an outage into a mystery")
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("round robin cycles in strict order")
    void roundRobinRotates() {
        LoadBalancer balancer = new RoundRobinBalancer(three());

        List<String> chosen = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            chosen.add(balancer.choose("ignored").id());
        }

        assertThat(chosen).containsExactly("a", "b", "c", "a", "b", "c");
    }

    @Test
    @DisplayName("round robin stays even over many requests")
    void roundRobinIsEven() {
        LoadBalancer balancer = new RoundRobinBalancer(three());
        Map<String, Integer> counts = new HashMap<>();

        for (int i = 0; i < 3_000; i++) {
            counts.merge(balancer.choose("x").id(), 1, Integer::sum);
        }

        assertThat(counts).containsOnlyKeys("a", "b", "c");
        assertThat(counts.values()).allSatisfy(n -> assertThat(n).isEqualTo(1_000));
    }

    @Test
    @DisplayName("weights are honoured: 3:2:1 over six requests")
    void weightedDistribution() {
        LoadBalancer balancer = new WeightedRoundRobinBalancer(List.of(
                new Backend("big", 3), new Backend("mid", 2), new Backend("small", 1)));

        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < 600; i++) {
            counts.merge(balancer.choose("x").id(), 1, Integer::sum);
        }

        assertThat(counts.get("big")).isEqualTo(300);
        assertThat(counts.get("mid")).isEqualTo(200);
        assertThat(counts.get("small")).isEqualTo(100);
    }

    @Test
    @DisplayName("weighting adapts when a backend dies - the expansion is not cached")
    void weightedRespondsToHealthChanges() {
        Backend big = new Backend("big", 3);
        Backend small = new Backend("small", 1);
        LoadBalancer balancer = new WeightedRoundRobinBalancer(List.of(big, small));

        balancer.choose("x");
        big.markUnhealthy();

        for (int i = 0; i < 20; i++) {
            assertThat(balancer.choose("x").id()).isEqualTo("small");
        }
    }

    @Test
    @DisplayName("least connections steers away from a busy backend")
    void leastConnectionsAdapts() {
        List<Backend> backends = three();
        LoadBalancer balancer = new LeastConnectionsBalancer(backends);

        // "a" is stuck on five slow requests.
        for (int i = 0; i < 5; i++) {
            backends.get(0).openConnection();
        }
        backends.get(1).openConnection();          // "b" has one

        assertThat(balancer.choose("x").id())
                .as("c is idle, so it should get the work")
                .isEqualTo("c");
    }

    @Test
    @DisplayName("least connections breaks ties deterministically")
    void leastConnectionsTieBreak() {
        assertThat(new LeastConnectionsBalancer(three()).choose("x").id()).isEqualTo("a");
    }

    @Test
    @DisplayName("hash routing is sticky: the same client always lands on the same backend")
    void hashIsSticky() {
        LoadBalancer balancer = new HashBalancer(three());

        String first = balancer.choose("session-abc").id();
        for (int i = 0; i < 100; i++) {
            assertThat(balancer.choose("session-abc").id()).isEqualTo(first);
        }
    }

    @Test
    @DisplayName("hash routing spreads different clients across the fleet")
    void hashSpreadsClients() {
        LoadBalancer balancer = new HashBalancer(three());
        Map<String, Integer> counts = new HashMap<>();

        for (int i = 0; i < 3_000; i++) {
            counts.merge(balancer.choose("session-" + i).id(), 1, Integer::sum);
        }

        assertThat(counts).hasSize(3);
        assertThat(counts.values()).allSatisfy(n -> assertThat(n).isBetween(700, 1_300));
    }

    @Test
    @DisplayName("and here is its weakness: one backend dying remaps almost everyone")
    void hashReshufflesOnFailure() {
        List<Backend> backends = new ArrayList<>(three());
        LoadBalancer balancer = new HashBalancer(backends);

        Map<String, String> before = new HashMap<>();
        for (int i = 0; i < 2_000; i++) {
            String client = "session-" + i;
            before.put(client, balancer.choose(client).id());
        }

        backends.get(2).markUnhealthy();

        long moved = before.entrySet().stream()
                .filter(e -> !"c".equals(e.getValue()))
                .filter(e -> !balancer.choose(e.getKey()).id().equals(e.getValue()))
                .count();

        System.out.printf("  losing 1 of 3 backends remapped %,d clients that were NOT on it%n", moved);

        assertThat(moved)
                .as("""
                        This is modulo hashing's resharding problem, wearing a load balancer hat.
                        Clients that had nothing to do with the failed backend get moved anyway.
                        Day 56's ring is the fix - this test exists to make you want it.""")
                .isGreaterThan(0);
    }
}
