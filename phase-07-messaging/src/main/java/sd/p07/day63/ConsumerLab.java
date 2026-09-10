package sd.p07.day63;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Day 63 - consumer groups, and the delivery semantics that follow from one line of ordering.
 *
 * <p><b>Consumer groups.</b> Every partition is assigned to exactly one consumer within a group.
 * So a group scales up to - and no further than - the partition count. A group with more
 * consumers than partitions has idle members, which is why partition count is a capacity
 * decision you make early and change painfully.
 *
 * <p>Different groups are independent: each gets every message. One topic can feed analytics,
 * billing and search simultaneously, because the log is not consumed by reading.
 *
 * <p><b>Delivery semantics.</b> Both of the methods below poll, process and commit. The only
 * difference is the ORDER of the last two steps, and that order is the entire difference between
 * losing data and duplicating it:
 *
 * <ul>
 *   <li><b>Process, then commit</b> - if you crash in between, the offset was never advanced, so
 *       the next consumer reprocesses. <b>At-least-once</b>: duplicates, never loss.</li>
 *   <li><b>Commit, then process</b> - if you crash in between, the offset moved past a record you
 *       never handled. <b>At-most-once</b>: loss, never duplicates.</li>
 * </ul>
 *
 * <p>There is no third option, and this is the practical meaning of "exactly-once does not
 * exist". You pick which failure you prefer, and then you make the consequence harmless -
 * at-least-once plus idempotent processing, which is Day 74. Almost every real system chooses
 * that, because a duplicate you can absorb beats a message you cannot recover.
 *
 * <p>Note that Kafka's default {@code enable.auto.commit=true} commits on a timer, in the
 * background, whether or not your processing succeeded. That default is neither of the two
 * semantics above: it is at-most-once wearing a disguise, and it is the reason so many teams
 * discover they were losing messages.
 */
public final class ConsumerLab {

    private ConsumerLab() {
    }

    /** What a consumer actually managed to do before its simulated crash. */
    public record Delivery(List<String> processed, boolean committed) {
    }

    /**
     * TODO(day63): AT-LEAST-ONCE. Poll up to {@code max} records, process them, then commit.
     *
     * <p>Subscribe, poll until you have {@code max} records or the timeout expires, collect each
     * record's value into the processed list, and then - only then - call
     * {@code consumer.commitSync()}.
     *
     * <p>When {@code crashBeforeCommit} is true, return without committing. That models a process
     * dying between the work and the acknowledgement, which is the failure this ordering is
     * designed to survive.
     *
     * <p>Report whether the commit actually happened in the returned {@link Delivery}.
     */
    public static Delivery atLeastOnce(KafkaConsumer<String, String> consumer, String topic,
                                       int max, Duration timeout, boolean crashBeforeCommit) {
        throw new UnsupportedOperationException("TODO(day63): process, then commit");
    }

    /**
     * TODO(day63): AT-MOST-ONCE. Poll up to {@code max} records, commit, then process.
     *
     * <p>Same polling loop, but {@code commitSync()} comes FIRST, before anything is added to the
     * processed list.
     *
     * <p>When {@code crashAfterCommit} is true, return immediately after committing, with an
     * empty processed list. The offset has advanced past records nobody handled - they are gone,
     * and nothing anywhere will ever tell you.
     */
    public static Delivery atMostOnce(KafkaConsumer<String, String> consumer, String topic,
                                      int max, Duration timeout, boolean crashAfterCommit) {
        throw new UnsupportedOperationException("TODO(day63): commit, then process");
    }

    /**
     * TODO(day63): which partitions did the group give this consumer?
     *
     * <p>Subscribe, then poll in a loop until {@code consumer.assignment()} is non-empty or the
     * timeout expires, and return the assignment.
     *
     * <p>The loop is necessary and instructive: assignment is not immediate. Subscribing only
     * expresses interest; the group coordinator has to run a rebalance, and until it completes
     * the consumer owns nothing. This is why a consumer that polls once and gives up appears to
     * see an empty topic.
     */
    public static Set<TopicPartition> awaitAssignment(KafkaConsumer<String, String> consumer,
                                                      String topic, Duration timeout) {
        throw new UnsupportedOperationException("TODO(day63): subscribe and poll until assigned");
    }

    // ---------------------------------------------------------------- given

    static List<String> values(ConsumerRecords<String, String> records) {
        List<String> values = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records) {
            values.add(record.value());
        }
        return values;
    }
}
