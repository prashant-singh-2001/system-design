package sd.p08.day77;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO(day77): the resource that actually enforces safety.
 *
 * <p>This is the half people skip, and skipping it is why "we use a distributed lock" is not by
 * itself a correctness argument.
 *
 * <p>{@code write} accepts a value only if its fencing token is greater than or equal to the
 * highest token already accepted. A lower token means the writer is working from a stale lease -
 * somebody else has held the lock since - so the write is rejected and counted.
 *
 * <p>Note where the enforcement lives: at the <b>resource</b>, not the lock service. The lock
 * service cannot know whether a client froze between checking and writing. The resource sees the
 * writes and can order them, so it is the only component that can make this guarantee.
 *
 * <p>Real systems do this: HBase and ZooKeeper-based designs pass epoch numbers, and object stores
 * offer conditional writes for the same reason.
 */
public final class FencedResource {

    private final List<String> writes = new ArrayList<>();
    private long highestTokenSeen;
    private int rejectedWrites;

    public boolean write(long fencingToken, String value) {
        throw new UnsupportedOperationException("TODO(day77): reject any token below the high water mark");
    }

    public List<String> writes() {
        return List.copyOf(writes);
    }

    public int rejectedWrites() {
        return rejectedWrites;
    }

    public long highestTokenSeen() {
        return highestTokenSeen;
    }
}
