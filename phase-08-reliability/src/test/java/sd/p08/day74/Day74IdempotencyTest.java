package sd.p08.day74;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day74IdempotencyTest {

    private InMemoryIdempotencyStore store;
    private PaymentService payments;

    @BeforeEach
    void setUp() {
        store = new InMemoryIdempotencyStore();
        payments = new PaymentService(store);
    }

    @Test
    @DisplayName("a first charge goes through to the gateway")
    void firstAttempt() {
        PaymentResult result = payments.charge("key-1", "ord-1", 5_000);

        assertThat(result.orderId()).isEqualTo("ord-1");
        assertThat(result.amountCents()).isEqualTo(5_000);
        assertThat(result.replayed()).isFalse();
        assertThat(payments.gatewayCalls()).isEqualTo(1);
    }

    @Test
    @DisplayName("THE point: retrying the same key charges the card exactly once")
    void retriesDoNotDoubleCharge() {
        PaymentResult first = payments.charge("key-1", "ord-1", 5_000);

        for (int i = 0; i < 20; i++) {
            PaymentResult retry = payments.charge("key-1", "ord-1", 5_000);

            assertThat(retry.paymentId())
                    .as("the client must get back the SAME payment id, not just a success")
                    .isEqualTo(first.paymentId());
            assertThat(retry.replayed()).isTrue();
        }

        assertThat(payments.gatewayCalls())
                .as("""
                        Twenty-one requests, one charge. The client could not tell whether its first
                        attempt succeeded, so it retried - and the server made that safe.""")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("different keys are different operations")
    void differentKeysChargeSeparately() {
        payments.charge("key-1", "ord-1", 5_000);
        payments.charge("key-2", "ord-1", 5_000);

        assertThat(payments.gatewayCalls())
                .as("""
                        Charging the same order twice on purpose is two operations and needs two
                        keys. The key covers a logical intent, not a request.""")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("a failed attempt releases its claim, so the retry can proceed")
    void failedAttemptsAreRetryable() {
        assertThatThrownBy(() -> payments.charge("key-1", "ord-1", -1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(store.claimCount())
                .as("""
                        A key claimed by an attempt that failed must not block the retry forever,
                        or a transient failure becomes a permanent one.""")
                .isZero();

        PaymentResult retry = payments.charge("key-1", "ord-1", 5_000);
        assertThat(retry.replayed()).isFalse();
        assertThat(payments.gatewayCalls()).isEqualTo(1);
    }

    @Test
    @DisplayName("concurrent attempts with one key: exactly one reaches the gateway")
    void concurrentAttempts() throws Exception {
        int attempts = 50;
        AtomicInteger rejected = new AtomicInteger();
        AtomicInteger succeeded = new AtomicInteger();

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<String>> calls = new ArrayList<>();
            for (int i = 0; i < attempts; i++) {
                calls.add(() -> {
                    try {
                        payments.charge("key-1", "ord-1", 5_000);
                        succeeded.incrementAndGet();
                        return "ok";
                    } catch (PaymentService.ConcurrentAttemptException e) {
                        rejected.incrementAndGet();
                        return "in-flight";
                    }
                });
            }
            for (Future<String> future : pool.invokeAll(calls)) {
                future.get();
            }
        }

        System.out.printf("  %d concurrent attempts -> %d gateway call, %d told to retry%n",
                attempts, payments.gatewayCalls(), rejected.get());

        assertThat(payments.gatewayCalls())
                .as("""
                        This is why claim() must be atomic. A containsKey-then-put has a window
                        wide enough for two retries to both proceed - reintroducing the exact bug
                        idempotency exists to prevent.""")
                .isEqualTo(1);
        assertThat(succeeded.get() + rejected.get()).isEqualTo(attempts);
    }

    @Test
    @DisplayName("after the work completes, later attempts replay rather than being rejected")
    void replayAfterCompletion() {
        payments.charge("key-1", "ord-1", 5_000);

        PaymentResult replay = payments.charge("key-1", "ord-1", 5_000);

        assertThat(replay.replayed())
                .as("a completed key replays; only an in-flight key is refused")
                .isTrue();
    }
}
