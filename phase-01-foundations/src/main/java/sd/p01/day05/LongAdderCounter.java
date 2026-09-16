package sd.p01.day05;

import java.util.concurrent.atomic.LongAdder;

/**
 * TODO(day05): use {@code LongAdder}.
 *
 * <p>The trick: instead of one contended cell, keep an array of cells and let threads hash
 * to different ones. Writers stop fighting over a single cache line. {@code sum()} adds the
 * cells up when you ask.
 *
 * <p>The trade: reads are no longer atomic snapshots and cost O(cells). That is exactly the
 * right bargain for metrics counters - written constantly, read once a scrape interval - and
 * the wrong one if you need to read-and-act on the value.
 *
 * <p>This "shard the contended thing" move reappears at every scale in this course: it is
 * also sharded databases and partitioned Kafka topics.
 */
public final class LongAdderCounter implements Counter {

    LongAdder longAdder = new LongAdder();

    @Override
    public void increment() {
        longAdder.increment();
    }

    @Override
    public long value() {
        return longAdder.sum();
    }

    @Override
    public String strategy() {
        return "LongAdder";
    }
}
