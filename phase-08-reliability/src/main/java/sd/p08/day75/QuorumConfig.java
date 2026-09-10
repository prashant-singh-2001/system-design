package sd.p08.day75;

/**
 * TODO(day75): the arithmetic that decides whether a leaderless system is consistent.
 *
 * <p>With N replicas, W write acknowledgements and R read responses:
 *
 * <blockquote>{@code W + R > N} guarantees that every read set overlaps every write set.</blockquote>
 *
 * <p>That overlap is the entire mechanism. If the sets must share at least one replica, and that
 * replica has the latest write, then the read can see it. Nothing subtler is going on.
 *
 * <p>The knobs are yours to turn per operation, which is the real appeal:
 * <ul>
 *   <li>{@code W=N, R=1} - fast reads, slow and fragile writes (any replica down blocks writes)</li>
 *   <li>{@code W=1, R=N} - fast writes, slow and fragile reads</li>
 *   <li>{@code W=R=(N+1)/2} - the balanced default. With N=3, W=R=2: one replica can be down and
 *       both reads and writes still work</li>
 * </ul>
 *
 * <p>The caveat worth carrying into an interview: <b>quorum overlap alone does not give you
 * linearizability.</b> Concurrent writes can still be applied in different orders at different
 * replicas, and a failed write may have reached some of them. You also need read repair,
 * versioning, or a consensus protocol on top. Saying "we use W+R>N so it is strongly consistent"
 * is a common and confident mistake.
 *
 * <p>Validate: N >= 1, and W and R each in [1, N].
 */
public record QuorumConfig(int replicas, int writeQuorum, int readQuorum) {

    public QuorumConfig {
        if (replicas < 1 || writeQuorum < 1 || readQuorum < 1
                || writeQuorum > replicas || readQuorum > replicas) {
            throw new IllegalArgumentException(
                    "need 1 <= W,R <= N: " + replicas + "/" + writeQuorum + "/" + readQuorum);
        }
    }

    /** TODO(day75): does this configuration guarantee read/write overlap? */
    public boolean guaranteesOverlap() {
        throw new UnsupportedOperationException("TODO(day75): W + R > N");
    }

    /** TODO(day75): how many replicas can be down while writes still succeed. */
    public int writeFaultTolerance() {
        throw new UnsupportedOperationException("TODO(day75): N - W");
    }

    /** TODO(day75): how many replicas can be down while reads still succeed. */
    public int readFaultTolerance() {
        throw new UnsupportedOperationException("TODO(day75): N - R");
    }

    /** The balanced default: W = R = majority. */
    public static QuorumConfig balanced(int replicas) {
        int majority = (replicas + 1) / 2;
        return new QuorumConfig(replicas, majority, majority);
    }
}
