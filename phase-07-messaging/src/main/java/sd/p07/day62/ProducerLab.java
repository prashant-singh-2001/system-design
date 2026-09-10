package sd.p07.day62;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import java.time.Duration;
import java.util.Map;

/**
 * Day 62 - what the producer promises you, and what it charges for the promise.
 *
 * <p>The {@code acks} setting is the durability dial, and it is the clearest cost/benefit trade
 * in the whole broker:
 *
 * <ul>
 *   <li><b>acks=0</b> - fire and forget. The producer does not wait for the broker at all.
 *       Fastest, and you do not even learn where the record went - the returned metadata has no
 *       offset. A network blip silently loses data. Only for telemetry you can afford to lose.</li>
 *   <li><b>acks=1</b> - the partition leader has written it. Fast, and you get an offset back.
 *       But if the leader dies before a follower replicates, that record is gone. This was the
 *       old default and the source of a great many "we lost messages" incidents.</li>
 *   <li><b>acks=all</b> - every in-sync replica has it. Survives a leader failure. Costs a round
 *       trip to the slowest in-sync replica.</li>
 * </ul>
 *
 * <p>Then <b>idempotence</b>, which fixes a subtler problem. A producer that sends, times out,
 * and retries may have had the first attempt succeed - so the retry creates a duplicate. An
 * idempotent producer tags each batch with a producer id and sequence number, and the broker
 * discards a repeat. Kafka refuses to enable it with {@code acks=1}, because deduplication is
 * meaningless if the record can vanish with a dead leader. Notice that Kafka enforces the
 * coherent combination rather than trusting you - a good design instinct to steal.
 *
 * <p>And <b>batching</b>: {@code linger.ms} makes the producer wait briefly to fill a batch.
 * Deliberately adding latency to gain throughput - Day 1's "batching beats chatter", as a
 * config knob.
 */
public final class ProducerLab {

    private ProducerLab() {
    }

    /** What the producer learned about a send. With acks=0 it learns almost nothing. */
    public record SendOutcome(int partition, long offset, boolean offsetKnown) {
    }

    /**
     * TODO(day62): send one record, block for the acknowledgement, and report what came back.
     *
     * <p>{@code producer.send(record).get()} yields a {@link RecordMetadata}. Its
     * {@code hasOffset()} tells you whether the broker actually reported a position - which is
     * exactly the difference {@code acks=0} makes, and the point of this method.
     *
     * <p>Wrap {@code InterruptedException} by restoring the interrupt flag and throwing
     * {@code IllegalStateException}; let an {@code ExecutionException} surface as
     * {@code IllegalStateException} too. Swallowing a send failure is how data disappears.
     */
    public static SendOutcome sendAndDescribe(KafkaProducer<String, String> producer,
                                              String topic, String key, String value) {
        throw new UnsupportedOperationException("TODO(day62): send, get, read the RecordMetadata");
    }

    /**
     * TODO(day62): send {@code count} records without blocking on each one, then {@code flush()}
     * and return the elapsed time.
     *
     * <p>This is the shape that lets batching work at all. Calling {@code get()} after every
     * send serialises the whole thing into one round trip per record, and no {@code linger.ms}
     * setting on earth can help you. Fire them all, then flush once.
     *
     * <p>Blocking per record is the single most common reason a Kafka producer is slow, and it
     * is invisible in a code review unless you know to look for it.
     */
    public static Duration sendBatch(KafkaProducer<String, String> producer, String topic,
                                     int count) {
        throw new UnsupportedOperationException("TODO(day62): send all, then flush once");
    }

    // ---------------------------------------------------------------- given

    /** Reads one producer metric by name, e.g. {@code batch-size-avg}. */
    public static double metric(KafkaProducer<String, String> producer, String name) {
        for (Map.Entry<MetricName, ? extends Metric> entry : producer.metrics().entrySet()) {
            if (entry.getKey().name().equals(name)
                    && entry.getKey().group().equals("producer-metrics")) {
                Object value = entry.getValue().metricValue();
                return value instanceof Double d ? d : Double.NaN;
            }
        }
        return Double.NaN;
    }

    static ProducerRecord<String, String> record(String topic, String key, String value) {
        return new ProducerRecord<>(topic, key, value);
    }
}
