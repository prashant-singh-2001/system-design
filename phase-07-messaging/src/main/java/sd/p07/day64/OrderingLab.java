package sd.p07.day64;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Day 64 - the ordering guarantee, stated exactly.
 *
 * <blockquote>
 * Kafka guarantees ordering <b>within a partition</b>. Across partitions there is no ordering
 * at all.
 * </blockquote>
 *
 * <p>That single sentence has enormous consequences, and half of them are counter-intuitive
 * until you see them fail.
 *
 * <p>Because the KEY chooses the partition, the key is your ordering unit. Key by
 * {@code orderId} and every event for that order is strictly ordered - created before paid before
 * shipped - while different orders proceed in parallel. Key by nothing and records round-robin
 * across partitions, so a consumer may see "shipped" before "created" and there is no bug to
 * find: you never asked for ordering.
 *
 * <p>This makes partition key choice a <b>correctness</b> decision, not a performance one, and
 * it is the single most consequential line in most event-driven designs.
 *
 * <p>The trade-off is the same one from Day 49's sharding, because it is literally the same
 * mechanism: ordering requires that related records go to one partition, and one partition is
 * handled by one consumer. Perfect ordering means no parallelism. Global ordering means one
 * partition, which means one consumer, which means your throughput ceiling is a single machine.
 * You almost never want that - you want ordering per entity, which is what keying gives you.
 */
public final class OrderingLab {

    private OrderingLab() {
    }

    /**
     * TODO(day64): publish an ordered sequence of events, all under the SAME key.
     *
     * <p>Send each value in order as {@code new ProducerRecord<>(topic, key, value)}, blocking on
     * each send so the order is unambiguous. Return the partition every record landed on - they
     * must all be the same one, which is exactly the point.
     *
     * <p>(Blocking per send is deliberately the wrong thing for throughput, as Day 62 showed. It
     * is the right thing here because we are demonstrating ordering, not speed.)
     */
    public static int publishKeyed(KafkaProducer<String, String> producer, String topic,
                                   String key, List<String> valuesInOrder) {
        throw new UnsupportedOperationException("TODO(day64): same key, same partition, in order");
    }

    /**
     * TODO(day64): publish values with NO key at all.
     *
     * <p>Use {@code new ProducerRecord<>(topic, value)} - the two-argument form, no key. Kafka
     * then spreads records across partitions, so their relative order is no longer preserved on
     * read. Return the set of partitions used, so the test can show they scattered.
     */
    public static List<Integer> publishUnkeyed(KafkaProducer<String, String> producer, String topic,
                                               List<String> values) {
        throw new UnsupportedOperationException("TODO(day64): no key means no ordering unit");
    }

    /**
     * TODO(day64): drain the topic and return the values grouped by partition, preserving the
     * order in which each partition delivered them.
     *
     * <p>Subscribe, poll until you have {@code expected} records or time out, and accumulate into
     * a {@code LinkedHashMap<Integer, List<String>>} keyed by
     * {@code record.partition()}.
     *
     * <p>Grouping by partition is what makes the guarantee visible: within each list the order is
     * exactly what was produced, while the interleaving BETWEEN lists is arbitrary.
     */
    public static Map<Integer, List<String>> drainByPartition(KafkaConsumer<String, String> consumer,
                                                              String topic, int expected,
                                                              Duration timeout) {
        throw new UnsupportedOperationException("TODO(day64): group the delivered values by partition");
    }
}
