package sd.p08.day71;

/** Which side of a network partition a node found itself on. */
public enum ReplicaSide {

    /** The side holding a quorum. It can still reach a majority of the cluster. */
    MAJORITY,

    /** The side that cannot reach a majority. Cut off, but still receiving client requests. */
    MINORITY
}
