package sd.p08.day76;

import java.util.OptionalInt;

/**
 * TODO(day76): Raft leader election.
 *
 * <p>Not to memorise a protocol - to make consensus concrete. Afterwards every managed service
 * that quietly depends on it (etcd, ZooKeeper, Kafka's controller, every cloud database's
 * failover) reads differently.
 *
 * <p><b>Terms</b> are the key idea, and they are simpler than they look. A term is a logical clock
 * that only ever increases. Each term has at most one leader. A node seeing a term higher than its
 * own immediately becomes a follower and adopts that term - which is how a leader that was
 * partitioned away discovers it has been replaced, without any global clock.
 *
 * <p><b>Election.</b> A follower that hears no heartbeat before its timeout increments its term,
 * becomes a CANDIDATE, votes for itself, and asks everyone else for a vote. A majority makes it
 * leader.
 *
 * <p><b>One vote per term.</b> This is what guarantees at most one leader. A majority is required
 * to win, and two majorities of the same set must overlap, so two candidates cannot both win the
 * same term - the overlapping node would have had to vote twice. That is the same overlap argument
 * as yesterday's quorum, doing a different job.
 *
 * <p><b>Randomised timeouts.</b> If every follower timed out simultaneously they would all become
 * candidates, split the vote, and nobody would win - repeatedly. Randomising the timeout makes one
 * node reliably go first. That is Day 72's jitter again: whenever independent actors share a
 * deadline, add noise. It is remarkable how often this idea is the fix.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code startElection} - increment term, become CANDIDATE, vote for self, reset the count
 *       to one.</li>
 *   <li>{@code handleVoteRequest} - if the request's term is LOWER than ours, refuse and report
 *       our term. If it is HIGHER, adopt it and revert to FOLLOWER (clearing our vote). Then grant
 *       the vote only if we have not already voted this term.</li>
 *   <li>{@code receiveVote} - ignore a response from an older term. If it carries a higher term,
 *       step down. Otherwise count a granted vote, and become LEADER on reaching a majority of
 *       {@code clusterSize}.</li>
 *   <li>{@code receiveHeartbeat} - from a leader whose term is at least ours: adopt the term,
 *       become FOLLOWER, record the leader. From an older term: ignore it (that leader is stale).</li>
 * </ul>
 */
public final class RaftNode {

    private final int id;
    private final int clusterSize;

    private NodeState state = NodeState.FOLLOWER;
    private long currentTerm;
    private OptionalInt votedFor = OptionalInt.empty();
    private OptionalInt knownLeader = OptionalInt.empty();
    private int votesReceived;

    public RaftNode(int id, int clusterSize) {
        if (clusterSize < 1) {
            throw new IllegalArgumentException("a cluster needs at least one node");
        }
        this.id = id;
        this.clusterSize = clusterSize;
    }

    public void startElection() {
        throw new UnsupportedOperationException("TODO(day76): bump the term and stand");
    }

    public VoteResponse handleVoteRequest(VoteRequest request) {
        throw new UnsupportedOperationException("TODO(day76): one vote per term");
    }

    public void receiveVote(VoteResponse response) {
        throw new UnsupportedOperationException("TODO(day76): count votes, win on a majority");
    }

    public void receiveHeartbeat(int leaderId, long term) {
        throw new UnsupportedOperationException("TODO(day76): a valid leader suppresses elections");
    }

    /** A majority of the cluster. With 5 nodes that is 3. */
    public int majority() {
        return clusterSize / 2 + 1;
    }

    public int id() {
        return id;
    }

    public NodeState state() {
        return state;
    }

    public long currentTerm() {
        return currentTerm;
    }

    public OptionalInt votedFor() {
        return votedFor;
    }

    public OptionalInt knownLeader() {
        return knownLeader;
    }

    public int votesReceived() {
        return votesReceived;
    }
}
