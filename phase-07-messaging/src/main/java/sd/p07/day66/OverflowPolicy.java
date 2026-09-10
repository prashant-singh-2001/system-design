package sd.p07.day66;

/**
 * What to do when the queue is full. There is no fourth option, and pretending otherwise is how
 * services fall over.
 */
public enum OverflowPolicy {

    /**
     * Make the producer wait. Backpressure propagates upstream: the producer slows to the
     * consumer's rate, nothing is lost, and latency rises. The right default when the producer
     * is something you control.
     */
    BLOCK,

    /**
     * Refuse the newest item. Latency stays bounded and you shed load deliberately. The right
     * choice when the producer is the outside world and you would rather serve some users well
     * than all users badly.
     */
    DROP_NEWEST,

    /**
     * Refuse the oldest item to make room. For data where fresh beats complete - live telemetry,
     * price ticks, sensor readings - where an old value has no value.
     */
    DROP_OLDEST
}
