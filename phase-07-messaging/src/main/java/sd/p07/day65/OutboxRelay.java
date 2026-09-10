package sd.p07.day65;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO(day65): the relay - the second half of the outbox pattern.
 *
 * <p>{@code fetchUnpublished} - select unpublished outbox rows, <b>ordered by id ascending</b>,
 * limited to {@code batchSize}. Order matters: the id sequence is the order the events happened
 * in, and publishing them out of order would hand consumers a lie.
 *
 * <p>{@code relayOnce} - for each unpublished row: publish to Kafka keyed by
 * {@code aggregateId} (so Day 64's ordering guarantee applies per aggregate), block for the
 * acknowledgement, and only then mark the row published. Return how many were relayed.
 *
 * <p><b>Publish before marking, never the reverse.</b> Crash in between and the row is published
 * again next time - a duplicate, which consumers can absorb. Mark first and crash, and the event
 * is lost forever with no trace. This is Day 63's at-least-once ordering, at a different layer,
 * and the reasoning is identical: choose the failure you can recover from.
 *
 * <p>{@code markPublished} - flip the flag for one row.
 */
public final class OutboxRelay {

    private final Connection connection;
    private final KafkaProducer<String, String> producer;
    private final String topic;

    public OutboxRelay(Connection connection, KafkaProducer<String, String> producer, String topic) {
        this.connection = connection;
        this.producer = producer;
        this.topic = topic;
    }

    public List<OutboxRecord> fetchUnpublished(int batchSize) throws SQLException {
        throw new UnsupportedOperationException("TODO(day65): unpublished rows, oldest id first");
    }

    public int relayOnce(int batchSize) throws SQLException {
        throw new UnsupportedOperationException("TODO(day65): publish, ack, then mark published");
    }

    public void markPublished(long outboxId) throws SQLException {
        throw new UnsupportedOperationException("TODO(day65): set published = true for this row");
    }

    // ---------------------------------------------------------------- given

    public static final String SCHEMA = """
            CREATE TABLE IF NOT EXISTS orders (
                id            TEXT PRIMARY KEY,
                customer_id   TEXT   NOT NULL,
                total_cents   BIGINT NOT NULL CHECK (total_cents > 0)
            );
            CREATE TABLE IF NOT EXISTS outbox (
                id            BIGSERIAL PRIMARY KEY,
                aggregate_id  TEXT    NOT NULL,
                event_type    TEXT    NOT NULL,
                payload       TEXT    NOT NULL,
                published     BOOLEAN NOT NULL DEFAULT FALSE,
                created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
            );
            CREATE INDEX IF NOT EXISTS outbox_unpublished
                ON outbox (id) WHERE published = FALSE;
            """;
}
