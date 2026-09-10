package sd.p01.day05;

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

    @Override
    public void increment() {
        throw new UnsupportedOperationException("TODO(day05): use a LongAdder");
    }

    @Override
    public long value() {
        throw new UnsupportedOperationException("TODO(day05): use a LongAdder");
    }

    @Override
    public String strategy() {
        return "LongAdder";
    }
}
