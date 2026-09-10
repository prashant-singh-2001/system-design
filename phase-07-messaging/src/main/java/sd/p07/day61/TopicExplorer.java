package sd.p07.day61;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Day 61 - what a Kafka topic actually is.
 *
 * <p>Not a queue. A **partitioned, append-only log**. That one sentence explains nearly every
 * behaviour that surprises people:
 *
 * <ul>
 *   <li>Reading does not remove anything. Messages stay until a retention policy drops them, so
 *       two independent consumer groups can read the same topic without interfering. A queue
 *       cannot do that.</li>
 *   <li>A consumer's position is just an <b>offset</b> - a number it remembers. Rewinding is
 *       setting that number backwards, which is why replay is trivial here and impossible with a
 *       traditional queue.</li>
 *   <li>A topic is split into <b>partitions</b>, and a partition is the unit of both parallelism
 *       and ordering. Ordering holds within a partition and nowhere else. That is Day 64.</li>
 * </ul>
 *
 * <p>Writes are sequential appends to a file, which is why Kafka is fast on cheap disks - the
 * same Day 1 fact that produced the LSM tree on Day 47.
 */
public final class TopicExplorer {

    private TopicExplorer() {
    }

    /** Where one produced record landed. */
    public record Placement(String key, int partition, long offset) {
    }

    /**
     * TODO(day61): send each key/value and return where the broker actually put it.
     *
     * <p>{@code producer.send(record)} returns a {@code Future<RecordMetadata>}. Calling
     * {@code get()} on it blocks until the broker acknowledges and hands back the partition and
     * offset - which is exactly what makes the placement observable.
     *
     * <p>Build a {@link ProducerRecord} with the topic, key and value. Kafka hashes the KEY to
     * choose the partition, so equal keys always land together. That is the mechanism behind
     * every ordering guarantee you will rely on for the rest of this phase.
     */
    public static List<Placement> publish(KafkaProducer<String, String> producer, String topic,
                                          Map<String, String> keyedMessages) {
        throw new UnsupportedOperationException("TODO(day61): send, then read the RecordMetadata");
    }

    /**
     * TODO(day61): subscribe and drain up to {@code expected} records.
     *
     * <p>{@code consumer.subscribe(List.of(topic))}, then {@code poll(Duration)} in a loop
     * collecting records until you have {@code expected} of them or the overall deadline passes.
     *
     * <p>Two things worth internalising while you write it:
     * <ul>
     *   <li>The FIRST poll usually returns nothing - it triggers the group join and partition
     *       assignment. Polling once and concluding the topic is empty is the single most common
     *       Kafka mistake.</li>
     *   <li>{@code poll} is not a peek. It is the heartbeat that keeps this consumer in the
     *       group. A consumer that stops polling is considered dead and its partitions are
     *       reassigned - which is why slow processing inside the poll loop causes rebalances.</li>
     * </ul>
     */
    public static List<ConsumerRecord<String, String>> drain(KafkaConsumer<String, String> consumer,
                                                             String topic, int expected,
                                                             Duration timeout) {
        throw new UnsupportedOperationException("TODO(day61): subscribe, then poll until drained");
    }
}
