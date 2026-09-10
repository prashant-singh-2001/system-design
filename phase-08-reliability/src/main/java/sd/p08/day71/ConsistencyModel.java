package sd.p08.day71;

/**
 * Ordered strongest to weakest. Stronger costs latency and availability, so the skill is picking
 * the WEAKEST model that satisfies the requirement - and being able to say why it is sufficient.
 */
public enum ConsistencyModel {

    /** Every read sees the most recent write; the system behaves like one copy. Consensus per write. */
    LINEARIZABLE,

    /** Causally related operations are seen in order; concurrent ones may differ per replica. */
    CAUSAL,

    /** A client always sees its own writes. Others may lag. */
    READ_YOUR_WRITES,

    /** A client never sees time go backwards, though it may lag behind the present. */
    MONOTONIC_READS,

    /** Replicas converge if writes stop. Almost free. */
    EVENTUAL
}
