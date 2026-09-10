package sd.p08.day76;

/** The three roles in Raft. Every node is exactly one of them at any moment. */
public enum NodeState {

    /** The default. Waits for a leader's heartbeat; if none arrives, becomes a CANDIDATE. */
    FOLLOWER,

    /** Standing for election in some term, collecting votes. */
    CANDIDATE,

    /** Won a majority. Sends heartbeats to suppress further elections. */
    LEADER
}
