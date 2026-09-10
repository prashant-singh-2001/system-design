package sd.p07.day67;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO(day67): dead-letter queues, poison messages, and bounded retries.
 *
 * <p>A <b>poison message</b> is one that fails every single time - malformed JSON, a reference to
 * a deleted row, a bug triggered by one specific input. With at-least-once delivery and unbounded
 * retries, that message is redelivered forever. It blocks its partition (Day 64: one partition,
 * one consumer, strict order), so a single bad record halts every message behind it. Your
 * consumer lag climbs, your alerts fire, and the cause is one row nobody can see.
 *
 * <p>The remedy has two halves and you need both:
 * <ul>
 *   <li><b>Bounded retries.</b> Try a few times - transient failures are real and usually
 *       resolve - but stop.</li>
 *   <li><b>A dead-letter queue.</b> Move the exhausted message aside so the pipeline continues,
 *       and keep it for a human. A DLQ is not a bin; it is an inbox.</li>
 * </ul>
 *
 * <p>The operational rule worth carrying: <b>a DLQ with no alert on it is a silent data-loss
 * mechanism.</b> Messages go there and nobody looks. Alert on depth, always.
 *
 * <p>Implement {@code consume}: for each message, attempt {@code handler.handle}. On success,
 * count it and move on. On a thrown exception, retry up to {@code maxAttempts} TOTAL attempts;
 * if all of them fail, add the message to the dead-letter list with its attempt count and carry
 * on to the next message. One bad message must never stop the batch.
 *
 * <p>Return a {@link ConsumptionReport} with what succeeded, what was dead-lettered, and the
 * total number of handler invocations - that last number makes the retry behaviour visible.
 */
public final class RetryingConsumer {

    private final int maxAttempts;
    private final List<Message> deadLetterQueue = new ArrayList<>();

    public RetryingConsumer(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("need at least one attempt");
        }
        this.maxAttempts = maxAttempts;
    }

    public record ConsumptionReport(List<String> succeeded, List<Message> deadLettered,
                                    int handlerInvocations) {
    }

    public ConsumptionReport consume(List<Message> messages, MessageHandler handler) {
        throw new UnsupportedOperationException("TODO(day67): retry, then dead-letter, then continue");
    }

    /** The DLQ contents. Alert on the size of this - always. */
    public List<Message> deadLetterQueue() {
        return List.copyOf(deadLetterQueue);
    }

    /**
     * TODO(day67): replay - the reason a DLQ is an inbox rather than a bin.
     *
     * <p>Once the bug is fixed or the missing row restored, re-run everything currently in the
     * DLQ through the handler, clearing the queue first so that anything still failing is
     * re-added rather than duplicated.
     *
     * <p>Getting that ordering wrong gives you a DLQ that doubles in size every replay, which is
     * a memorable way to learn the lesson.
     */
    public ConsumptionReport replayDeadLetters(MessageHandler handler) {
        throw new UnsupportedOperationException("TODO(day67): drain the DLQ back through the handler");
    }
}
