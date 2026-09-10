package sd.p07.day67;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day67DeadLetterTest {

    private static final List<Message> BATCH = List.of(
            Message.of("m1", "good"),
            Message.of("m2", "POISON"),
            Message.of("m3", "good"),
            Message.of("m4", "good"));

    /** Fails on anything marked POISON, succeeds otherwise. */
    private static MessageHandler poisonAware(AtomicInteger calls) {
        return message -> {
            calls.incrementAndGet();
            if (message.payload().contains("POISON")) {
                throw new IllegalStateException("cannot parse: " + message.id());
            }
        };
    }

    @Test
    @DisplayName("a poison message is retried, then set aside - and the batch continues")
    void poisonMessageIsDeadLettered() {
        AtomicInteger calls = new AtomicInteger();
        RetryingConsumer consumer = new RetryingConsumer(3);

        RetryingConsumer.ConsumptionReport report = consumer.consume(BATCH, poisonAware(calls));

        assertThat(report.succeeded())
                .as("""
                        One bad message must never stop the batch. Without this, a single
                        unparseable record blocks its partition and every message behind it.""")
                .containsExactly("m1", "m3", "m4");
        assertThat(report.deadLettered()).extracting(Message::id).containsExactly("m2");
        assertThat(consumer.deadLetterQueue()).hasSize(1);
    }

    @Test
    @DisplayName("retries are bounded - three attempts, not forever")
    void retriesAreBounded() {
        AtomicInteger calls = new AtomicInteger();
        new RetryingConsumer(3).consume(List.of(Message.of("m1", "POISON")), poisonAware(calls));

        assertThat(calls)
                .as("unbounded retries on a permanently broken message is an infinite loop")
                .hasValue(3);
    }

    @Test
    @DisplayName("the dead-lettered message records how many attempts it consumed")
    void attemptCountIsRecorded() {
        RetryingConsumer consumer = new RetryingConsumer(4);
        consumer.consume(List.of(Message.of("m1", "POISON")), m -> {
            throw new IllegalStateException("nope");
        });

        assertThat(consumer.deadLetterQueue().get(0).attempts())
                .as("the operator needs to know it was genuinely retried, not dropped on first sight")
                .isEqualTo(4);
    }

    @Test
    @DisplayName("a transient failure that later succeeds is not dead-lettered")
    void transientFailuresRecover() {
        AtomicInteger attempts = new AtomicInteger();
        RetryingConsumer consumer = new RetryingConsumer(3);

        RetryingConsumer.ConsumptionReport report =
                consumer.consume(List.of(Message.of("m1", "flaky")), message -> {
                    if (attempts.incrementAndGet() < 3) {
                        throw new IllegalStateException("temporarily unavailable");
                    }
                });

        assertThat(report.succeeded()).containsExactly("m1");
        assertThat(consumer.deadLetterQueue())
                .as("most failures really are transient - that is why we retry at all")
                .isEmpty();
        assertThat(report.handlerInvocations()).isEqualTo(3);
    }

    @Test
    @DisplayName("handler invocations make the retry cost visible")
    void invocationsAreCounted() {
        AtomicInteger calls = new AtomicInteger();
        RetryingConsumer.ConsumptionReport report =
                new RetryingConsumer(3).consume(BATCH, poisonAware(calls));

        // three good messages once each, plus the poison one three times
        assertThat(report.handlerInvocations()).isEqualTo(6);
        assertThat(calls).hasValue(6);
    }

    @Test
    @DisplayName("replay: a DLQ is an inbox, not a bin")
    void replayAfterTheFixIsDeployed() {
        RetryingConsumer consumer = new RetryingConsumer(2);
        consumer.consume(BATCH, poisonAware(new AtomicInteger()));

        assertThat(consumer.deadLetterQueue()).hasSize(1);

        // The bug is fixed. Everything now parses.
        RetryingConsumer.ConsumptionReport replay = consumer.replayDeadLetters(message -> {
        });

        assertThat(replay.succeeded()).containsExactly("m2");
        assertThat(consumer.deadLetterQueue())
                .as("a successful replay must empty the queue")
                .isEmpty();
    }

    @Test
    @DisplayName("a replay that still fails re-queues once - it does not double the DLQ")
    void failedReplayDoesNotDuplicate() {
        RetryingConsumer consumer = new RetryingConsumer(2);
        consumer.consume(BATCH, poisonAware(new AtomicInteger()));

        consumer.replayDeadLetters(message -> {
            throw new IllegalStateException("still broken");
        });

        assertThat(consumer.deadLetterQueue())
                .as("""
                        Clear the queue before replaying. Get the ordering wrong and the DLQ
                        doubles on every replay - a memorable way to learn this.""")
                .hasSize(1);
    }

    @Test
    @DisplayName("an all-good batch dead-letters nothing")
    void happyPath() {
        RetryingConsumer consumer = new RetryingConsumer(3);
        RetryingConsumer.ConsumptionReport report = consumer.consume(
                List.of(Message.of("a", "ok"), Message.of("b", "ok")), message -> {
                });

        assertThat(report.succeeded()).containsExactly("a", "b");
        assertThat(report.handlerInvocations()).isEqualTo(2);
        assertThat(consumer.deadLetterQueue()).isEmpty();
    }

    @Test
    @DisplayName("zero attempts is not a retry policy")
    void rejectsNonsense() {
        assertThatThrownBy(() -> new RetryingConsumer(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
