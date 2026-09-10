package sd.p07.day70;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Day 70 - the phase review. Every piece from days 61-69, wired into one pipeline:
 *
 * <pre>
 *   API -> [orders + outbox, one transaction] -> relay -> Kafka -> consumer -> projection
 * </pre>
 *
 * <p>Each hop uses something you built:
 * <ul>
 *   <li><b>Day 65</b> - the order and its event are written atomically, so the event exists if
 *       and only if the order does.</li>
 *   <li><b>Day 62</b> - the relay publishes with {@code acks=all} and an idempotent producer, so
 *       a retry cannot duplicate a batch at the broker.</li>
 *   <li><b>Day 64</b> - events are keyed by customer, so one customer's events stay ordered while
 *       different customers proceed in parallel.</li>
 *   <li><b>Day 63</b> - the consumer processes before committing: at-least-once, so nothing is
 *       lost.</li>
 *   <li><b>Day 74, borrowed early</b> - because delivery is at-least-once, the projection must be
 *       idempotent. It tracks which event ids it has already applied and ignores repeats.</li>
 * </ul>
 *
 * <p>That last point is the phase's conclusion, and it is worth stating as a slogan:
 * <b>exactly-once DELIVERY does not exist; exactly-once PROCESSING does - through idempotency.</b>
 * Every reliable event-driven system is at-least-once delivery plus idempotent consumers. Anyone
 * selling you the first is selling you the second with worse marketing.
 */
public final class OrderPipeline {

    private final Connection connection;
    private final KafkaProducer<String, String> producer;
    private final String topic;

    public OrderPipeline(Connection connection, KafkaProducer<String, String> producer,
                         String topic) {
        this.connection = connection;
        this.producer = producer;
        this.topic = topic;
    }

    /**
     * TODO(day70): place an order - the write side.
     *
     * <p>One transaction: insert into {@code orders}, insert into {@code outbox} with
     * {@code published = false}, commit. Roll back and rethrow on any failure, and restore
     * autoCommit in a finally block.
     *
     * <p>The outbox payload should be {@code customerId + ":" + totalCents} - deliberately a
     * trivial format, because the point today is the plumbing, not the serialization.
     */
    public void placeOrder(String orderId, String customerId, long totalCents) throws SQLException {
        throw new UnsupportedOperationException("TODO(day70): order + outbox, one transaction");
    }

    /**
     * TODO(day70): the relay - publish unpublished outbox rows, oldest first.
     *
     * <p>Key each record by CUSTOMER id, not order id. That choice is the whole of Day 64 applied:
     * it guarantees one customer's events arrive in order, while different customers proceed in
     * parallel. Keying by order id would give you ordering you do not need and lose the ordering
     * you do.
     *
     * <p>Publish, block for the acknowledgement, then mark the row published - never the other
     * way round. Return how many were relayed.
     */
    public int relay(int batchSize) throws SQLException {
        throw new UnsupportedOperationException("TODO(day70): publish, ack, then mark");
    }

    /**
     * TODO(day70): the read side - consume events into a revenue-per-customer projection,
     * idempotently.
     *
     * <p>Poll until {@code expected} records have been seen or the timeout expires. For each
     * record:
     * <ul>
     *   <li>Read the event id from the record's header {@code event-id} (the relay sets it).</li>
     *   <li>If {@code processedEventIds} already contains that id, count a suppressed duplicate
     *       and skip it. This is the idempotency that makes at-least-once safe.</li>
     *   <li>Otherwise parse {@code customerId:totalCents}, add to the customer's running total,
     *       and record the id as processed.</li>
     * </ul>
     *
     * <p>Commit only after processing the batch - at-least-once, as on Day 63.
     *
     * <p>Return the projection and the duplicate count in a {@link ConsumeResult}.
     */
    public ConsumeResult consumeIntoProjection(KafkaConsumer<String, String> consumer,
                                               int expected, Duration timeout,
                                               java.util.Set<String> processedEventIds) {
        throw new UnsupportedOperationException("TODO(day70): consume idempotently into a projection");
    }

    public record ConsumeResult(Map<String, Long> revenueByCustomer, int consumed,
                                int duplicatesSuppressed) {
    }

    // ---------------------------------------------------------------- given

    public static final String SCHEMA = """
            CREATE TABLE IF NOT EXISTS orders (
                id          TEXT PRIMARY KEY,
                customer_id TEXT   NOT NULL,
                total_cents BIGINT NOT NULL CHECK (total_cents > 0)
            );
            CREATE TABLE IF NOT EXISTS outbox (
                id           BIGSERIAL PRIMARY KEY,
                aggregate_id TEXT    NOT NULL,
                payload      TEXT    NOT NULL,
                published    BOOLEAN NOT NULL DEFAULT FALSE
            );
            """;

    /** The header name the relay writes and the consumer reads for deduplication. */
    public static final String EVENT_ID_HEADER = "event-id";
}
