package sd.p06.day53;

/**
 * A cache is bounded, so something has to go. This interface is the decision of what.
 *
 * <p>Strategy again (Day 12): the cache holds the mechanism, the policy holds the choice.
 */
public interface EvictionPolicy {

    void recordInsert(String key);

    void recordAccess(String key);

    void remove(String key);

    /** The key to evict next, or {@code null} if the policy is tracking nothing. */
    String evictionCandidate();

    String name();
}
